package com.exoleviathan.mychat.firebase.fcm

import com.exoleviathan.mychat.utility.FCM_MESSAGE_PREFERENCE_ID
import com.exoleviathan.mychat.utility.FCM_TOKEN_KEY
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import com.exoleviathan.mychat.utility.SharedPreferenceHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMNotificationService: FirebaseMessagingService() {
    private lateinit var preferenceHelper: SharedPreferenceHelper

    override fun onNewToken(token: String) {
        Logger.d(TAG, "onNewToken", "token: $token", ModuleNames.FIREBASE_API.value)

        saveRegistrationToken(token)
    }

    private fun saveRegistrationToken(token: String) {
        Logger.d(TAG, "saveRegistrationToken", "token: $token", ModuleNames.FIREBASE_API.value)

        preferenceHelper = SharedPreferenceHelper(this, FCM_MESSAGE_PREFERENCE_ID)
        preferenceHelper.putItem(FCM_TOKEN_KEY, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Logger.d(TAG, "onMessageReceived", "message: ${message.notification?.title}", ModuleNames.FIREBASE_API.value)
    }

    companion object {
        private const val TAG = "FirebaseMessageService"
    }
}