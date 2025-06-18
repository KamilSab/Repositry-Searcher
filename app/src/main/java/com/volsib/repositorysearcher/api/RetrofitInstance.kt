package com.volsib.repositorysearcher.api

import com.volsib.repositorysearcher.util.Constants.Companion.BASE_URL
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {
    companion object {

        private val retrofit by lazy {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(getHeaderInterceptor())
                .build()
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }

        val api: ReposService by lazy {
            retrofit.create(ReposService::class.java)
        }

        private fun getHeaderInterceptor(): Interceptor {
            return Interceptor { chain ->
                val request =
                    chain.request().newBuilder()
                        .header("Accept", "application/vnd.github+json")
                        .header("X-GitHub-Api-Version", "2022-11-28")
                        .build()
                chain.proceed(request)
            }
        }
    }
}