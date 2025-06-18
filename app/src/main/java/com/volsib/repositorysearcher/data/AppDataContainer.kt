package com.volsib.repositorysearcher.data

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

class AppDataContainer(
    private val context: Context,
    override val application: Application
) : AppContainer {
    
    private val database: RepoDatabase by lazy {
        RepoDatabase.getDatabase(context)
    }
    
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    }
    
    override val authRepository: AuthRepository by lazy {
        AuthRepository(database.userDao(), sharedPreferences)
    }
    
    override val reposRepository: ReposRepository by lazy {
        ReposRepository(
            database.repoDao(),
            database.searchHistoryDao(),
            authRepository
        )
    }
}