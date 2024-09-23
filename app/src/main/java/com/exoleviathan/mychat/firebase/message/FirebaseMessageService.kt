package com.exoleviathan.mychat.firebase.message

import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import com.google.firebase.messaging.FirebaseMessagingService

class FirebaseMessageService: FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Logger.d(TAG, "onNewToken", "token: $token", ModuleNames.FIREBASE_API.value)

        sendRegistrationToServer()
    }

    private fun sendRegistrationToServer() {

    }

    companion object {
        private const val TAG = "FirebaseMessageService"
    }
}