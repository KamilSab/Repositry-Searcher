package com.volsib.repositorysearcher.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val email: String,
    val password: String, // Простое хранение пароля без хеширования
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long? = null
) 