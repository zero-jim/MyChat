package com.exoleviathan.mychat.firebase.fcm

import android.content.Context
import okhttp3.ResponseBody
import retrofit2.Callback

interface FCMNotificationSenderApi {
    @Throws(Exception::class)
    suspend fun sendNotification(context: Context, msg: String, callback: Callback<ResponseBody?>)
}