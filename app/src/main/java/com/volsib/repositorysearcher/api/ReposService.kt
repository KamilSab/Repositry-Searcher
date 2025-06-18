package com.volsib.repositorysearcher.api

import com.volsib.repositorysearcher.models.Repo
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ReposService {

    @GET("/users/{username}/repos")
    suspend fun getReposByUsername(
        @Path("username") username: String,
        @Query("page") page: Int,
    ): Response<MutableList<Repo>>

    @GET("/repos/{username}/{repo_name}/zipball/")
    suspend fun downloadRepo(
        @Path("username") username: String,
        @Path("repo_name") repoName: String,
    ): Response<ResponseBody>
}
