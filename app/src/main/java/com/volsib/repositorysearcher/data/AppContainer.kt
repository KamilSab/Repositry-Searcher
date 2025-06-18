package com.volsib.repositorysearcher.data

import android.app.Application

interface AppContainer {
    val application: Application
    val authRepository: AuthRepository
    val reposRepository: ReposRepository
}

