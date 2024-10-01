package com.exoleviathan.mychat.firebase.fcm

import com.exoleviathan.mychat.firebase.auth.FirebaseAuthHelper
import com.exoleviathan.mychat.firebase.firestore.FirebaseFirestoreHelper
import com.exoleviathan.mychat.utility.FCM_MESSAGE_PREFERENCE_ID
import com.exoleviathan.mychat.utility.FCM_TOKEN_KEY
import com.exoleviathan.mychat.utility.FCM_TOKEN_UPDATED_KEY
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import com.exoleviathan.mychat.utility.SharedPreferenceHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FCMNotificationService : FirebaseMessagingService() {
    private lateinit var preferenceHelper: SharedPreferenceHelper

    override fun onNewToken(token: String) {
        Logger.d(TAG, "onNewToken", "token: $token", ModuleNames.FIREBASE_API.value)

        saveRegistrationToken(token)
    }

    private fun saveRegistrationToken(token: String) {
        Logger.d(TAG, "saveRegistrationToken", "token: $token", ModuleNames.FIREBASE_API.value)

        preferenceHelper = SharedPreferenceHelper(this, FCM_MESSAGE_PREFERENCE_ID)
        preferenceHelper.putItem(FCM_TOKEN_UPDATED_KEY, false)
        preferenceHelper.putItem(FCM_TOKEN_KEY, token)

        val userId = FirebaseAuthHelper.getInstance().getAuth().uid
        Logger.d(TAG, "saveRegistrationToken", "userId: $userId", ModuleNames.FIREBASE_API.value)

        userId?.let {

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    FirebaseFirestoreHelper.getInstance().saveFCMToken(userId, token) {
                        Logger.i(TAG, "saveRegistrationToken", "save FCM token task completed: ${it.isSuccessful}", ModuleNames.FIREBASE_API.value)

                        if (it.isSuccessful) {
                            preferenceHelper.putItem(FCM_TOKEN_UPDATED_KEY, true)
                        }
                    }
                } catch (ex: Exception) {
                    Logger.e(TAG, "saveRegistrationToken", "failed to save FCM token, error: ${ex.message}", ModuleNames.FIREBASE_API.value)
                    ex.printStackTrace()
                }
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Logger.d(TAG, "onMessageReceived", "message: ${message.notification?.title}", ModuleNames.FIREBASE_API.value)

        // TODO: create custom notification and show user
    }

    companion object {
        private const val TAG = "FirebaseMessageService"
    }
}