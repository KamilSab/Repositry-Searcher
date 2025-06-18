package com.volsib.repositorysearcher.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.volsib.repositorysearcher.models.Repo

@Dao
interface RepoDao {
    @Insert
    suspend fun insert(repo: Repo): Long

    @Query("SELECT * FROM repos")
    fun getAllRepos(): LiveData<List<Repo>>
    
    @Query("SELECT * FROM repos WHERE userId = :userId ORDER BY downloadDate DESC")
    fun getReposByUser(userId: Int): LiveData<List<Repo>>
    
    @Query("SELECT * FROM repos WHERE userId = :userId AND isFavorite = 1 ORDER BY downloadDate DESC")
    fun getFavoriteRepos(userId: Int): LiveData<List<Repo>>
    
    @Update
    suspend fun updateRepo(repo: Repo)
    
    @Query("UPDATE repos SET isFavorite = :isFavorite WHERE id = :repoId")
    suspend fun updateFavoriteStatus(repoId: Int, isFavorite: Boolean)
    
    @Query("DELETE FROM repos WHERE userId = :userId")
    suspend fun deleteUserRepos(userId: Int)
}