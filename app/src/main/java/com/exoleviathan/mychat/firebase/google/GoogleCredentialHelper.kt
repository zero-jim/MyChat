package com.exoleviathan.mychat.firebase.google

import android.content.Context
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import com.google.auth.oauth2.GoogleCredentials

class GoogleCredentialHelper private constructor() : GoogleCredentialApi {
    private var accessTokenValue: String? = null

    @Throws(Exception::class)
    override suspend fun getServerAccessToken(context: Context, tokenValue: (String?) -> Unit) {
        Logger.d(TAG, "getServerAccessToken", moduleName = ModuleNames.FIREBASE_API.value)

        accessTokenValue ?: run {
            val file = context.applicationContext.assets.open(SERVER_AUTH_FILE)
            val googleCredential = GoogleCredentials.fromStream(file).createScoped(listOf(GOOGLE_SERVER_READ_WRITE_SCOPE))

            val accessToken = googleCredential?.accessToken?.let {
                googleCredential.refreshIfExpired()
                googleCredential.accessToken
            } ?: run {
                googleCredential?.refresh()
                googleCredential?.accessToken
            }

            accessTokenValue = accessToken?.tokenValue
        }

        Logger.d(TAG, "getServerAccessToken", "accessTokenValue: $accessTokenValue")
        tokenValue.invoke(accessTokenValue)
    }

    companion object {
        private const val TAG = "GoogleCredentialHelper"
        private const val SERVER_AUTH_FILE = "server_auth.json"
        private const val GOOGLE_SERVER_READ_WRITE_SCOPE = "https://www.googleapis.com/auth/cloud-platform"

        private var instance: GoogleCredentialHelper? = null
        private val lock = Any()

        @Synchronized
        fun getInstance(): GoogleCredentialApi {
            synchronized(lock) {
                return instance ?: synchronized(lock) {
                    Logger.i(TAG, "getInstance", "instance is null, creating new instance", ModuleNames.FIREBASE_API.value)

                    GoogleCredentialHelper().also {
                        Logger.i(TAG, "getInstance", "setting up the created instance", ModuleNames.FIREBASE_API.value)
                        instance = it
                    }
                }
            }
        }
    }
}