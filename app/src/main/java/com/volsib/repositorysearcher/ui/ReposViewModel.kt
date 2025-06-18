package com.volsib.repositorysearcher.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.volsib.repositorysearcher.RSApplication
import com.volsib.repositorysearcher.data.ReposRepository
import com.volsib.repositorysearcher.models.Repo
import com.volsib.repositorysearcher.models.SearchHistory
import com.volsib.repositorysearcher.util.Resource
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File
import java.io.IOException


class ReposViewModel(
    application: Application,
    private val reposRepository: ReposRepository,
) : AndroidViewModel(application) {

    val repos: MutableLiveData<Resource<List<Repo>>?> = MutableLiveData()
    val downloads: MutableLiveData<Resource<String>> = MutableLiveData()
    val searchHistory: MutableLiveData<Resource<List<SearchHistory>>> = MutableLiveData()
    val favoriteRepos: MutableLiveData<Resource<List<Repo>>> = MutableLiveData()

    private var reposResponse: MutableList<Repo>? = null
    private var currentPage = 1
    var isLastPage = false

    private var oldUsername: String? = null
    private var newUsername: String? = null
    private var currentUsername: String? = null

    private var currentRepoName: String? = null
    private var searchHistoryLiveData: LiveData<List<SearchHistory>>? = null
    private var searchHistoryObserver: androidx.lifecycle.Observer<List<SearchHistory>>? = null

    override fun onCleared() {
        super.onCleared()
        // Удаляем наблюдатель при уничтожении ViewModel
        searchHistoryObserver?.let { observer ->
            searchHistoryLiveData?.removeObserver(observer)
        }
    }

    fun getRepos(username: String) = viewModelScope.launch {
        if (username.isNotEmpty()) {
            currentUsername = username
            safeReposCall(username)
        } else {
            invalidateInput()
            repos.postValue(Resource.Error("Введите непустое значение!"))
        }
    }

    fun downloadRepo(repo: Repo) = viewModelScope.launch {
        safeDownloadCall(repo)
    }
    
    fun getSearchHistory() {
        viewModelScope.launch {
            searchHistory.postValue(Resource.Loading())
            try {
                // Удаляем предыдущий наблюдатель, если он есть
                searchHistoryObserver?.let { observer ->
                    searchHistoryLiveData?.removeObserver(observer)
                }
                
                // Получаем новые данные
                searchHistoryLiveData = reposRepository.getSearchHistory()
                
                // Создаем и устанавливаем новый наблюдатель
                searchHistoryObserver = androidx.lifecycle.Observer { historyList ->
                    searchHistory.postValue(Resource.Success(historyList))
                }
                searchHistoryLiveData?.observeForever(searchHistoryObserver!!)
            } catch (e: Exception) {
                searchHistory.postValue(Resource.Error("Ошибка загрузки истории"))
            }
        }
    }
    
    fun clearSearchHistory() {
        viewModelScope.launch {
            try {
                reposRepository.clearSearchHistory()
                searchHistory.postValue(Resource.Success(emptyList()))
            } catch (e: Exception) {
                searchHistory.postValue(Resource.Error("Ошибка очистки истории"))
            }
        }
    }
    
    fun getFavoriteRepos() {
        viewModelScope.launch {
            favoriteRepos.postValue(Resource.Loading())
            try {
                val favorites = reposRepository.getFavoriteRepos()
                favorites.observeForever { favoritesList ->
                    favoriteRepos.postValue(Resource.Success(favoritesList))
                }
            } catch (e: Exception) {
                favoriteRepos.postValue(Resource.Error("Ошибка загрузки избранных"))
            }
        }
    }
    
    fun toggleFavorite(repo: Repo) {
        viewModelScope.launch {
            try {
                reposRepository.toggleFavorite(repo)
                // Обновляем список репозиториев после изменения состояния избранного
                currentUsername?.let { username ->
                    getRepos(username)
                }
            } catch (e: Exception) {
                // Обработка ошибки
            }
        }
    }

    private fun invalidateInput() {
        reposResponse = null
        currentPage = 1
        isLastPage = false
        oldUsername = null
        newUsername = null
    }

    fun getDownloadedRepos() = reposRepository.getAllRepos()

    private suspend fun safeReposCall(username: String) {
        newUsername = username

        if (oldUsername != newUsername) {
            currentPage = 1
        }

        repos.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = reposRepository.getReposByUsername(username, currentPage)
                val result = handleReposResponse(response)
                
                // Сохранение в историю при успешном поиске
                if (result is Resource.Success) {
                    reposRepository.saveSearchHistory(username, result.data!!.size)
                    // Обновляем историю поиска после сохранения
                    getSearchHistory()
                }
                
                repos.postValue(result)
            } else {
                repos.postValue(Resource.Error("Нет подключения к Интернету!"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> repos.postValue(Resource.Error("Сбой сети, повторите попытку позже"))
                else -> {
                    repos.postValue(Resource.Error("Ошибка при поиске: ${t.message}"))
                    t.printStackTrace()
                }
            }
        }
    }

    private suspend fun safeDownloadCall(repo: Repo) {
        downloads.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                currentRepoName = repo.name
                val response = reposRepository.downloadRepo(repo.owner?.login!!, repo.name!!)
                // Вставка скачанного репозитория в локальную бд как избранного
                reposRepository.insertRepo(repo.copy(isFavorite = true))
                downloads.postValue(handleDownloadResponse(response))
            } else {
                downloads.postValue(Resource.Error("Нет подключения к Интернету!"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> downloads.postValue(Resource.Error("Сбой сети, повторите попытку позже"))
                else -> {
                    downloads.postValue(Resource.Error("Ошибка загрузки!"))
                    t.printStackTrace()
                }
            }
        }
    }

    private fun handleDownloadResponse(response: Response<ResponseBody>): Resource<String> {
        if (response.isSuccessful) {
            val responseBody = response.body()
            responseBody?.byteStream()?.use { input ->
                val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val outputFile = File(path, "$currentRepoName.zip")
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
                return Resource.Success(currentRepoName.toString())
            }
        }

        invalidateInput()
        return Resource.Error("Ошибка загрузки!")
    }

    private fun handleReposResponse(response: Response<MutableList<Repo>>) : Resource<List<Repo>> {
        response.body()?.let { resultResponse ->
            // Если получили первую страницу или изменили логин
            if (reposResponse == null || oldUsername != newUsername) {
                currentPage = 1
                oldUsername = newUsername
                reposResponse = resultResponse
            // Добавляем данные, если еще есть следующие страницы
            } else if (!isLastPage){
                currentPage++
                reposResponse?.addAll(resultResponse)
            }

            // Определяем, есть ли следующая страница
            val linkHeader = response.headers()["link"]
            isLastPage = if (linkHeader != null) {
                !linkHeader.contains("rel=\"next\"")
            } else {
                true
            }

            return Resource.Success(reposResponse?: resultResponse)
        }

        // Обработка ошибок
        return when (response.code()) {
            404 -> Resource.Error("Пользователь не найден")
            403 -> Resource.Error("Превышен лимит запросов к API GitHub")
            else -> Resource.Error("Ошибка при поиске репозиториев: ${response.message()}")
        }
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}