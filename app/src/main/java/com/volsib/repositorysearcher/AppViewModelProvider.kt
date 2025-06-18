package com.volsib.repositorysearcher

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.volsib.repositorysearcher.data.AppContainer
import com.volsib.repositorysearcher.ui.ReposViewModel
import com.volsib.repositorysearcher.viewmodel.AuthViewModel
import com.volsib.repositorysearcher.ui.profile.ProfileViewModel

/**
 * Предоставляет фабрику для создания экземпляров ViewModel для всего приложения
 */
class AppViewModelProvider(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        // ReposViewModel
        initializer {
            val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application)
            ReposViewModel(application, appContainer.reposRepository)
        }
        // AuthViewModel
        initializer {
            AuthViewModel(appContainer.authRepository)
        }
        // ProfileViewModel
        initializer {
            ProfileViewModel(
                application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application,
                authRepository = appContainer.authRepository,
                reposRepository = appContainer.reposRepository
            )
        }
    }

    /**
     * Extension функция получает объект [Application] и возвращает экземпляр [RSApplication].
     */
    private fun CreationExtras.rsApplication(): RSApplication =
        (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RSApplication)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ReposViewModel::class.java) -> {
                ReposViewModel(appContainer.application, appContainer.reposRepository) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(
                    application = appContainer.application,
                    authRepository = appContainer.authRepository,
                    reposRepository = appContainer.reposRepository
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}