package com.exoleviathan.mychat.firebase.fcm

import android.content.Context
import com.exoleviathan.mychat.firebase.admin.FirebaseCredentialHelper
import com.exoleviathan.mychat.network.RetrofitClient
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FCMNotificationSender private constructor() {
    private val retrofitClient = RetrofitClient(SERVER_BASE_URL)

    private val responseCb = object : Callback<ResponseBody> {

        override fun onResponse(call: Call<ResponseBody>, reposne: Response<ResponseBody>) {
            Logger.i(TAG, "responseCb::onResponse", moduleName = ModuleNames.FIREBASE_API.value)
        }

        override fun onFailure(call: Call<ResponseBody>, throwable: Throwable) {
            Logger.i(TAG, "responseCb::onFailure", moduleName = ModuleNames.FIREBASE_API.value)
        }
    }

    fun sendNotification(context: Context, msg: String) {
        Logger.d(TAG, "sendNotification", "msg: $msg", ModuleNames.FIREBASE_API.value)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val authCredentials = FirebaseCredentialHelper.getInstance(context).getServerAccessToken()
                val requestBody = msg.toRequestBody("application/json".toMediaType())

                retrofitClient.getFcmApi()
                    ?.sendNotificationMessage("Bearer $authCredentials", requestBody)
                    ?.enqueue(responseCb)
            } catch (ex: Exception) {
                Logger.e(TAG, "sendNotification", "error: ${ex.message}", ModuleNames.FIREBASE_API.value)
                ex.printStackTrace()
            }
        }
    }

    companion object {
        private const val TAG = "FCMNotificationSender"
        private const val SERVER_BASE_URL = "https://fcm.googleapis.com/"

        private var instance: FCMNotificationSender? = null
        private val lock = Any()

        @Synchronized
        fun getInstance(): FCMNotificationSender {
            synchronized(lock) {
                return instance ?: run {
                    Logger.i(TAG, "getInstance", "instance is null", ModuleNames.FIREBASE_API.value)
                    FCMNotificationSender().also {
                        instance = it
                    }
                }
            }
        }
    }
}