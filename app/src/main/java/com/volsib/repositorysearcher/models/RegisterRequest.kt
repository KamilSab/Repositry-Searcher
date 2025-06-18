package com.volsib.repositorysearcher.models

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val confirmPassword: String
) 