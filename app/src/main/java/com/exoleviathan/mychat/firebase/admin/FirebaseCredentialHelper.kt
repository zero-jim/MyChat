package com.exoleviathan.mychat.firebase.admin

import android.content.Context
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import com.google.auth.oauth2.GoogleCredentials

class FirebaseCredentialHelper private constructor(context: Context) {
    private val googleCredential: GoogleCredentials?

    init {
        val file = context.applicationContext.assets.open(SERVER_AUTH_FILE)

        googleCredential = GoogleCredentials.fromStream(file)
            .createScoped(listOf(READ_WRITE_SCOPE))
    }

    fun getServerAccessToken(): String? {
        Logger.d(TAG, "getServerAccessToken", moduleName = ModuleNames.FIREBASE_API.value)

        val accessToken = googleCredential?.accessToken?.let {
            googleCredential.refreshIfExpired()
            googleCredential.accessToken
        } ?: run {
            googleCredential?.refresh()
            googleCredential?.accessToken
        }

        Logger.d(TAG, "getServerAccessToken", "token: ${accessToken?.tokenValue}")
        return accessToken?.tokenValue
    }

    companion object {
        private const val TAG = "FirebaseCredentialHelper"
        private const val SERVER_AUTH_FILE = "server_auth.json"
        private const val READ_WRITE_SCOPE = "https://www.googleapis.com/auth/cloud-platform"

        private var instance: FirebaseCredentialHelper? = null
        private val lock = Any()

        @Synchronized
        fun getInstance(context: Context): FirebaseCredentialHelper {
            synchronized(lock) {
                return instance ?: run {
                    Logger.i(TAG, "getInstance", "instance is null", ModuleNames.FIREBASE_API.value)
                    FirebaseCredentialHelper(context).also {
                        instance = it
                    }
                }
            }
        }
    }
}