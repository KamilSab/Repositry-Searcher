package com.volsib.repositorysearcher.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.volsib.repositorysearcher.models.SearchHistory

@Dao
interface SearchHistoryDao {
    @Insert
    suspend fun insertSearch(search: SearchHistory): Long
    
    @Query("SELECT * FROM search_history WHERE userId = :userId ORDER BY searchDate DESC LIMIT :limit")
    fun getRecentSearches(userId: Int, limit: Int = 10): LiveData<List<SearchHistory>>
    
    @Query("DELETE FROM search_history WHERE userId = :userId AND id NOT IN (SELECT id FROM search_history WHERE userId = :userId ORDER BY searchDate DESC LIMIT :limit)")
    suspend fun cleanOldSearches(userId: Int, limit: Int = 50)
    
    @Query("DELETE FROM search_history WHERE userId = :userId")
    suspend fun clearUserHistory(userId: Int)
} 