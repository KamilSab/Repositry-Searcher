package com.volsib.repositorysearcher.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.volsib.repositorysearcher.api.RetrofitInstance
import com.volsib.repositorysearcher.models.Repo
import com.volsib.repositorysearcher.models.SearchHistory
import okhttp3.ResponseBody
import retrofit2.Response

class ReposRepository(
    private val repoDao: RepoDao,
    private val searchHistoryDao: SearchHistoryDao,
    private val authRepository: AuthRepository
) {
    // Remote
    suspend fun getReposByUsername(username: String, page: Int): Response<MutableList<Repo>> {
        return RetrofitInstance.api.getReposByUsername(username, page)
    }

    suspend fun downloadRepo(username: String, repoName: String): Response<ResponseBody> {
        return RetrofitInstance.api.downloadRepo(username, repoName)
    }

    // Local
    suspend fun insertRepo(repo: Repo): Long {
        val currentUser = authRepository.getCurrentUser()
        val repoWithUser = repo.copy(userId = currentUser?.id)
        return repoDao.insert(repoWithUser)
    }

    fun getAllRepos(): LiveData<List<Repo>> {
        return repoDao.getAllRepos()
    }
    
    suspend fun getUserRepos(): LiveData<List<Repo>> {
        val currentUser = authRepository.getCurrentUser()
        return if (currentUser != null) {
            repoDao.getReposByUser(currentUser.id)
        } else {
            MutableLiveData(emptyList())
        }
    }
    
    suspend fun getFavoriteRepos(): LiveData<List<Repo>> {
        val currentUser = authRepository.getCurrentUser()
        return if (currentUser != null) {
            repoDao.getFavoriteRepos(currentUser.id)
        } else {
            MutableLiveData(emptyList())
        }
    }
    
    suspend fun toggleFavorite(repo: Repo) {
        val currentUser = authRepository.getCurrentUser()
        currentUser?.let { user ->
            val updatedRepo = repo.copy(
                userId = user.id,
                isFavorite = !repo.isFavorite
            )
            repoDao.updateRepo(updatedRepo)
        }
    }
    
    // История поиска
    suspend fun saveSearchHistory(query: String, resultsCount: Int) {
        val currentUser = authRepository.getCurrentUser()
        currentUser?.let { user ->
            val searchHistory = SearchHistory(
                userId = user.id,
                query = query,
                resultsCount = resultsCount
            )
            searchHistoryDao.insertSearch(searchHistory)
            // Очистка старых записей
            searchHistoryDao.cleanOldSearches(user.id)
        }
    }
    
    suspend fun getSearchHistory(): LiveData<List<SearchHistory>> {
        val currentUser = authRepository.getCurrentUser()
        return if (currentUser != null) {
            searchHistoryDao.getRecentSearches(currentUser.id)
        } else {
            MutableLiveData(emptyList())
        }
    }
    
    suspend fun clearSearchHistory() {
        val currentUser = authRepository.getCurrentUser()
        currentUser?.let { user ->
            searchHistoryDao.clearUserHistory(user.id)
        }
    }
}