package com.volsib.repositorysearcher.models

import com.google.gson.annotations.SerializedName

data class Owner(
    val login: String?,
    val id: Int?,
    @SerializedName("avatar_url")
    val avatarUrl: String?,
)
