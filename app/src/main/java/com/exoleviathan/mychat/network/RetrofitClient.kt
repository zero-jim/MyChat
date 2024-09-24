package com.exoleviathan.mychat.network

import com.exoleviathan.mychat.network.endpoint.FcmApi
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient(baseUrl: String) {
    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val interceptor = HttpLoggingInterceptor()
        .setLevel(HttpLoggingInterceptor.Level.BODY)

    private val client = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(client)
        .build()

    fun getFcmApi(): FcmApi? {
        return retrofit.create(FcmApi::class.java)
    }
}