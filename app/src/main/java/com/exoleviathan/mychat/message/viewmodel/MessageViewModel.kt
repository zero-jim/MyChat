package com.exoleviathan.mychat.message.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exoleviathan.mychat.firebase.auth.FirebaseAuthenticationHelper
import com.exoleviathan.mychat.firebase.fcm.FCMNotificationSender
import com.exoleviathan.mychat.firebase.firestore.FirebaseFirestoreHelper
import com.exoleviathan.mychat.firebase.model.FCMMessageData
import com.exoleviathan.mychat.firebase.model.FCMNotificationData
import com.exoleviathan.mychat.firebase.model.FCMPushNotificationData
import com.exoleviathan.mychat.firebase.model.MessageData
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import com.google.gson.Gson
import kotlinx.coroutines.launch

class MessageViewModel : ViewModel() {
    private val senderId: String? = FirebaseAuthenticationHelper.getInstance()?.getFirebaseAuth()?.uid
    private val senderName: String = FirebaseAuthenticationHelper.getInstance()?.getFirebaseAuth()?.currentUser?.displayName ?: ""
    private val _messageData = MutableLiveData<ArrayList<MessageData>>()
    val messageData: LiveData<ArrayList<MessageData>> = _messageData

    fun addMessageSnapshotListener(receiverId: String, onMessageFound: (Boolean) -> Unit) {
        Logger.d(TAG, "addMessageSnapshotListener", "senderId: $senderId receiverId: $receiverId", ModuleNames.FIREBASE_API.value)

        senderId?.let {
            FirebaseFirestoreHelper.getInstance()?.addMessageSnapshotListener(senderId, receiverId) {
                onMessageFound.invoke(true)
                _messageData.postValue(it)
            }
        }
    }

    fun removeMessageSnapshotListener(receiverId: String) {
        Logger.d(TAG, "removeMessageSnapshotListener", "senderId: $senderId receiverId: $receiverId", ModuleNames.FIREBASE_API.value)

        senderId?.let {
            FirebaseFirestoreHelper.getInstance()?.removeMessageSnapshotListener(senderId, receiverId)
        }
    }

    fun sendMessage(context: Context, receiverId: String, receiverName: String, message: String, sendMessageResult: (Boolean, String) -> Unit) {
        Logger.d(TAG, "sendMessage", "senderId: $senderId receiverId: $receiverId receiverName: $receiverName", ModuleNames.FIREBASE_API.value)

        senderId?.let {
            FirebaseFirestoreHelper.getInstance()?.sendMessage(it, senderName, receiverId, receiverName, message) { result, message ->
                sendMessageResult.invoke(result, message)

                if (result) {
                    viewModelScope.launch {
                        sendMessageNotification(context, receiverId, message)
                    }
                } else {
                    Logger.w(TAG, "sendMessage", "failed to send message", ModuleNames.MESSAGE.value)
                }
            }
        }
    }

    private fun sendMessageNotification(context: Context, receiverId: String, message: String) {
        FirebaseFirestoreHelper.getInstance()?.getFCMToken(receiverId) { token ->
            val data = FCMPushNotificationData(
                FCMMessageData(
                    token,
                    FCMNotificationData(
                        FirebaseAuthenticationHelper.getInstance()?.getUserInformation()?.displayName,
                        message
                    )
                )
            )

            val gson = Gson()
                .getAdapter(FCMPushNotificationData::class.java)
                .toJsonTree(data)
            FCMNotificationSender.getInstance().sendNotification(context, gson.toString())
        }
    }

    companion object {
        private const val TAG = "MessageViewModel"
    }
}