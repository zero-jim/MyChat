package com.exoleviathan.mychat.firebase.fcm

import android.content.Context
import com.exoleviathan.mychat.firebase.google.GoogleCredentialHelper
import com.exoleviathan.mychat.network.RetrofitClient
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Callback

class FCMNotificationSender private constructor() : FCMNotificationSenderApi {
    private val retrofitClient = RetrofitClient(SERVER_BASE_URL)

    @Throws(Exception::class)
    override suspend fun sendNotification(context: Context, msg: String, callback: Callback<ResponseBody?>) {
        Logger.d(TAG, "sendNotification", "msg: $msg", ModuleNames.FIREBASE_API.value)

        try {
            GoogleCredentialHelper.getInstance().getServerAccessToken(context) { authCredentials ->
                Logger.d(TAG, "sendNotification", "authCredentials: $authCredentials", ModuleNames.FIREBASE_API.value)

                val requestBody = msg.toRequestBody("application/json".toMediaType())
                val api = retrofitClient.getFcmApi()

                val call = api?.sendNotification("Bearer $authCredentials", requestBody)
                call?.enqueue(callback)
            }
        } catch (ex: Exception) {
            Logger.e(TAG, "sendNotification", "error: ${ex.message}", ModuleNames.FIREBASE_API.value)
            ex.printStackTrace()
        }
    }

    companion object {
        private const val TAG = "FCMNotificationSender"
        private const val SERVER_BASE_URL = "https://fcm.googleapis.com/"

        private var instance: FCMNotificationSender? = null
        private val lock = Any()

        @Synchronized
        fun getInstance(): FCMNotificationSenderApi {
            synchronized(lock) {
                return instance ?: synchronized(lock) {
                    Logger.i(TAG, "getInstance", "instance is null, creating new instance", ModuleNames.FIREBASE_API.value)

                    FCMNotificationSender().also {
                        Logger.i(TAG, "getInstance", "setting up the new instance", ModuleNames.FIREBASE_API.value)
                        instance = it
                    }
                }
            }
        }
    }
}