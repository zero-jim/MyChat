package com.exoleviathan.mychat.network.endpoint

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface FcmApi {
    @Headers("Content-Type:application/json")
    @POST("v1/projects/my-chat-a-p-p/messages:send")
    fun sendNotificationMessage(@Header("Authorization") authCredential: String?, @Body reqBody: RequestBody?): Call<ResponseBody>?
}