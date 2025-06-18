package com.volsib.repositorysearcher.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "repos",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Repo(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    val name: String?,
    val owner: Owner?,
    val description: String?,
    @SerializedName("html_url")
    val url: String?,
    @SerializedName("stargazers_count")
    val stargazersCount: Int?,
    val language: String?,
    val forks: Int?,
    val userId: Int? = null,
    val downloadDate: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)