package com.volsib.repositorysearcher.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "search_history",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SearchHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val query: String,
    val searchDate: Long = System.currentTimeMillis(),
    val resultsCount: Int = 0
) 