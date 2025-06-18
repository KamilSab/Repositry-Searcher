package com.volsib.repositorysearcher.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.volsib.repositorysearcher.models.User

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User): Long
    
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?
    
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?
    
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?
    
    @Update
    suspend fun updateUser(user: User)
    
    @Query("UPDATE users SET lastLoginAt = :timestamp WHERE id = :userId")
    suspend fun updateLastLogin(userId: Int, timestamp: Long)
} 