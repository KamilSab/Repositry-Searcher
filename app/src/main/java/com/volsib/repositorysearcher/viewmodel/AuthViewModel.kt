package com.volsib.repositorysearcher.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.volsib.repositorysearcher.data.AuthRepository
import com.volsib.repositorysearcher.models.LoginRequest
import com.volsib.repositorysearcher.models.RegisterRequest
import com.volsib.repositorysearcher.models.User
import com.volsib.repositorysearcher.util.Resource
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _authState = MutableLiveData<Resource<AuthState>>()
    val authState: LiveData<Resource<AuthState>> = _authState
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading()
            
            val result = authRepository.login(LoginRequest(username, password))
            _authState.value = when {
                result.isSuccess -> Resource.Success(AuthState.LoggedIn(result.getOrNull()!!))
                else -> Resource.Error(result.exceptionOrNull()?.message ?: "Ошибка авторизации")
            }
        }
    }
    
    fun register(username: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading()
            
            val result = authRepository.register(RegisterRequest(username, email, password, confirmPassword))
            _authState.value = when {
                result.isSuccess -> Resource.Success(AuthState.Registered(result.getOrNull()!!))
                else -> Resource.Error(result.exceptionOrNull()?.message ?: "Ошибка регистрации")
            }
        }
    }
    
    fun logout() {
        authRepository.logout()
        _authState.value = Resource.Success(AuthState.LoggedOut)
    }
    
    fun checkAuthState() {
        if (authRepository.isLoggedIn()) {
            viewModelScope.launch {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    _authState.value = Resource.Success(AuthState.LoggedIn(currentUser))
                } else {
                    _authState.value = Resource.Success(AuthState.LoggedOut)
                }
            }
        } else {
            _authState.value = Resource.Success(AuthState.LoggedOut)
        }
    }
}

sealed class AuthState {
    object LoggedOut : AuthState()
    data class LoggedIn(val user: User) : AuthState()
    data class Registered(val user: User) : AuthState()
} 