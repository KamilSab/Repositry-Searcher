package com.volsib.repositorysearcher.ui.profile

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.volsib.repositorysearcher.RSApplication
import com.volsib.repositorysearcher.data.AuthRepository
import com.volsib.repositorysearcher.data.ReposRepository
import com.volsib.repositorysearcher.models.Repo
import com.volsib.repositorysearcher.models.SearchHistory
import com.volsib.repositorysearcher.models.User
import com.volsib.repositorysearcher.util.Resource
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ProfileViewModel(
    application: Application,
    private val authRepository: AuthRepository,
    private val reposRepository: ReposRepository
) : AndroidViewModel(application) {

    private val _user = MutableLiveData<Resource<User>>()
    val user: LiveData<Resource<User>> = _user

    private val _searchHistory = MutableLiveData<Resource<List<SearchHistory>>>()
    val searchHistory: LiveData<Resource<List<SearchHistory>>> = _searchHistory

    private val _favoriteRepos = MutableLiveData<Resource<List<Repo>>>()
    val favoriteRepos: LiveData<Resource<List<Repo>>> = _favoriteRepos

    private var searchHistoryObserver: androidx.lifecycle.Observer<List<SearchHistory>>? = null
    private var favoriteReposObserver: androidx.lifecycle.Observer<List<Repo>>? = null
    private var searchHistoryLiveData: LiveData<List<SearchHistory>>? = null
    private var favoriteReposLiveData: LiveData<List<Repo>>? = null

    private val application = getApplication<Application>() as RSApplication

    init {
        loadUserData()
        viewModelScope.launch {
            loadSearchHistory()
            loadFavoriteRepos()
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchHistoryObserver?.let { observer ->
            searchHistoryLiveData?.removeObserver(observer)
        }
        favoriteReposObserver?.let { observer ->
            favoriteReposLiveData?.removeObserver(observer)
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _user.value = Resource.Loading()
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    _user.value = Resource.Success(currentUser)
                } else {
                    _user.value = Resource.Error("Пользователь не авторизован")
                }
            } catch (e: Exception) {
                _user.value = Resource.Error("Ошибка загрузки данных пользователя")
            }
        }
    }

    fun loadSearchHistory() {
        viewModelScope.launch {
            _searchHistory.value = Resource.Loading()
            try {
                // Удаляем предыдущий наблюдатель, если он есть
                searchHistoryObserver?.let { observer ->
                    searchHistoryLiveData?.removeObserver(observer)
                }
                
                // Получаем новые данные
                searchHistoryLiveData = reposRepository.getSearchHistory()
                
                // Создаем и устанавливаем новый наблюдатель
                searchHistoryObserver = androidx.lifecycle.Observer { historyList ->
                    _searchHistory.value = Resource.Success(historyList)
                }
                searchHistoryLiveData?.observeForever(searchHistoryObserver!!)
            } catch (e: Exception) {
                _searchHistory.value = Resource.Error("Ошибка загрузки истории")
            }
        }
    }

    fun loadFavoriteRepos() {
        viewModelScope.launch {
            _favoriteRepos.value = Resource.Loading()
            try {
                // Удаляем предыдущий наблюдатель, если он есть
                favoriteReposObserver?.let { observer ->
                    favoriteReposLiveData?.removeObserver(observer)
                }
                
                // Получаем новые данные
                favoriteReposLiveData = reposRepository.getFavoriteRepos()
                
                // Создаем и устанавливаем новый наблюдатель
                favoriteReposObserver = androidx.lifecycle.Observer { favoritesList ->
                    _favoriteRepos.value = Resource.Success(favoritesList)
                }
                favoriteReposLiveData?.observeForever(favoriteReposObserver!!)
            } catch (e: Exception) {
                _favoriteRepos.value = Resource.Error("Ошибка загрузки избранного")
            }
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            try {
                reposRepository.clearSearchHistory()
                _searchHistory.value = Resource.Success(emptyList())
            } catch (e: Exception) {
                _searchHistory.value = Resource.Error("Ошибка очистки истории")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
                _user.value = Resource.Error("Пользователь не авторизован")
            } catch (e: Exception) {
                // Обработка ошибки выхода
            }
        }
    }

    fun formatDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }

    fun openInBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        getApplication<Application>().startActivity(intent)
    }

    fun toggleFavorite(repo: Repo) {
        viewModelScope.launch {
            try {
                reposRepository.toggleFavorite(repo)
                // Перезагружаем список избранного после изменения
                loadFavoriteRepos()
            } catch (e: Exception) {
                // Обработка ошибки
            }
        }
    }
} 