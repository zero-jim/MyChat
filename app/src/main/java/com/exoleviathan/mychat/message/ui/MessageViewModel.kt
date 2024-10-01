package com.exoleviathan.mychat.message.ui

import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exoleviathan.mychat.firebase.auth.FirebaseAuthHelper
import com.exoleviathan.mychat.firebase.fcm.FCMNotificationSender
import com.exoleviathan.mychat.firebase.firestore.FirebaseFirestoreHelper
import com.exoleviathan.mychat.firebase.firestore.fields.FCMTokenDataFields
import com.exoleviathan.mychat.firebase.model.ChatRoomData
import com.exoleviathan.mychat.firebase.model.FCMMessageData
import com.exoleviathan.mychat.firebase.model.FCMNotificationData
import com.exoleviathan.mychat.firebase.model.FCMPushNotificationRequestData
import com.exoleviathan.mychat.firebase.model.MessageData
import com.exoleviathan.mychat.firebase.model.UserAuthData
import com.exoleviathan.mychat.message.model.MessageIntent
import com.exoleviathan.mychat.message.model.MessageState
import com.exoleviathan.mychat.message.model.MessageViewHolders
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date

class MessageViewModel : ViewModel() {
    val message = MutableLiveData<String?>(null)

    private var userAuthData: UserAuthData? = null

    val messageIntent = Channel<MessageIntent>(Channel.UNLIMITED)
    val messageState = MutableStateFlow<MessageState>(MessageState.Initial)

    private val messageObserver = Observer<String?> {
        Logger.d(TAG, "init::messageObserver", "text: $it", ModuleNames.MESSAGE.value)

        messageState.value = MessageState.IsSendButtonEnabled(TextUtils.isEmpty(it).not())
    }

    private val responseCb = object : Callback<ResponseBody?> {

        override fun onResponse(call: Call<ResponseBody?>, reposne: Response<ResponseBody?>) {
            Logger.i(TAG, "responseCb::onResponse", moduleName = ModuleNames.MESSAGE.value)
            messageState.value = MessageState.SendNotificationStatus(true)
        }

        override fun onFailure(call: Call<ResponseBody?>, throwable: Throwable) {
            Logger.i(TAG, "responseCb::onFailure", moduleName = ModuleNames.MESSAGE.value)
            messageState.value = MessageState.SendNotificationStatus(false)
        }
    }

    init {
        Logger.d(TAG, "init", moduleName = ModuleNames.MESSAGE.value)

        message.observeForever(messageObserver)

        viewModelScope.launch {

            messageIntent.consumeAsFlow().collect { intent ->
                Logger.i(TAG, "init", "intent: $intent", ModuleNames.MESSAGE.value)

                when (intent) {
                    is MessageIntent.AddMessageUpdateListener -> {
                        Logger.d(TAG, "init", "AddMessageUpdateListener::receiverId: ${intent.receiverId}", ModuleNames.MESSAGE.value)

                        viewModelScope.launch(Dispatchers.IO) {

                            userAuthData?.let {
                                Logger.i(TAG, "init", "AddMessageUpdateListener::userAuthData is available", ModuleNames.MESSAGE.value)
                                addMessageSnapshotListener(intent.receiverId)
                            } ?: run {
                                Logger.i(TAG, "init", "AddMessageUpdateListener::userAuthData is unavailable", ModuleNames.MESSAGE.value)

                                getUserAuthInformation {
                                    Logger.i(TAG, "init", "AddMessageUpdateListener::user information load success: $it", ModuleNames.MESSAGE.value)

                                    if (it.not()) {
                                        messageState.value = MessageState.FailedToLoadUserInformation("Failed to load user information.")
                                    } else {
                                        viewModelScope.launch(Dispatchers.IO) {
                                            addMessageSnapshotListener(intent.receiverId)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    MessageIntent.RemoveMessageUpdateListener -> {
                        removeMessageSnapshotListener()
                    }

                    is MessageIntent.SendMessage -> {
                        Logger.d(TAG, "init", "SendMessage::receiverId: ${intent.receiverId} receiverName: ${intent.receiverName}", ModuleNames.MESSAGE.value)

                        userAuthData?.let {
                            Logger.i(TAG, "init", "SendMessage::userAuthData is available", ModuleNames.MESSAGE.value)
                            sendMessage(intent.context, intent.receiverId, intent.receiverName)
                        } ?: run {
                            Logger.i(TAG, "init", "SendMessage::userAuthData is unavailable", ModuleNames.MESSAGE.value)

                            getUserAuthInformation {
                                Logger.i(TAG, "init", "SendMessage::user information load success: $it", ModuleNames.MESSAGE.value)

                                if (it.not()) {
                                    messageState.value = MessageState.FailedToLoadUserInformation("Failed to load user information.")
                                } else {
                                    viewModelScope.launch(Dispatchers.IO) {
                                        sendMessage(intent.context, intent.receiverId, intent.receiverName)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun getUserAuthInformation(isSuccess: (Boolean) -> Unit) {
        Logger.d(TAG, "getUserAuthInformation", moduleName = ModuleNames.MESSAGE.value)

        FirebaseAuthHelper.getInstance().getUserAuthInformation { authData ->
            Logger.d(TAG, "getUserAuthInformation", "authData: $authData", ModuleNames.MESSAGE.value)

            userAuthData = authData
            isSuccess.invoke(authData != null)
        }
    }

    private fun createChatRoomId(senderId: String, receiverId: String): String {
        Logger.d(TAG, "createChatRoomId", "senderId: $senderId receiverId: $receiverId", ModuleNames.MESSAGE.value)

        val chatRoomId = arrayOf(senderId, receiverId).sortedArray().joinToString(separator = "_")

        Logger.d(TAG, "createChatRoomId", "chatRoomId: $chatRoomId", ModuleNames.MESSAGE.value)
        return chatRoomId
    }

    private suspend fun updateChatRoomReadStatus(chatRoomId: String) {
        Logger.d(TAG, "updateChatRoomReadStatus", "chatRoomId: $chatRoomId", ModuleNames.MESSAGE.value)

        userAuthData?.let {
            FirebaseFirestoreHelper.getInstance().updateChatRoomReadStatus(chatRoomId, it.uid, true) { task ->
                Logger.i(TAG, "updateChatRoomReadStatus", "response: ${task.isSuccessful}")
            }
        }
    }

    private suspend fun addMessageSnapshotListener(receiverId: String) {
        Logger.d(TAG, "addMessageSnapshotListener", "receiverId: $receiverId", ModuleNames.MESSAGE.value)

        userAuthData?.let { authData ->
            val chatRoomId = createChatRoomId(authData.uid, receiverId)
            updateChatRoomReadStatus(chatRoomId)

            FirebaseFirestoreHelper.getInstance().addMessageUpdateListener(chatRoomId, TAG) { querySnaps, exception ->
                if (querySnaps == null || querySnaps.documentChanges.size == 0) {
                    messageState.value = MessageState.MessageListUpdated(arrayListOf())
                    Logger.w(TAG, "addMessageUpdateListener", "query snap is null or empty", ModuleNames.MESSAGE.value)
                    return@addMessageUpdateListener
                }

                if (exception != null) {
                    messageState.value = MessageState.MessageListUpdated(arrayListOf())
                    Logger.e(TAG, "addMessageSnapshotListener", "error to get message: ${exception.message}", ModuleNames.MESSAGE.value)
                    exception.printStackTrace()
                    return@addMessageUpdateListener
                }

                querySnaps.documentChanges.forEach { docChange ->
                    if (docChange.type == DocumentChange.Type.ADDED || docChange.type == DocumentChange.Type.REMOVED) {
                        val messageList = arrayListOf<MessageData?>()
                        querySnaps.documents.forEach { docSnap ->
                            val msgData = docSnap.toObject(MessageData::class.java)
                            Logger.d(TAG, "addMessageUpdateListener", "msgData: $msgData", ModuleNames.MESSAGE.value)
                            messageList.add(msgData)
                        }
                        Logger.i(TAG, "addMessageUpdateListener", "messageList size: ${messageList.size}", ModuleNames.MESSAGE.value)
                        messageState.value = MessageState.MessageListUpdated(messageList)
                    } else {
                        Logger.w(TAG, "addMessageSnapshotListener", "querySnapshots: ${docChange.type}", ModuleNames.MESSAGE.value)
                    }
                }
            }
        }
    }

    private fun removeMessageSnapshotListener() {
        Logger.d(TAG, "removeMessageSnapshotListener", moduleName = ModuleNames.MESSAGE.value)

        FirebaseFirestoreHelper.getInstance().removeMessageUpdateListener(TAG)
    }

    private suspend fun sendMessage(context: Context, receiverId: String, receiverName: String) {
        Logger.d(TAG, "sendMessage", "receiverId: $receiverId receiverName: $receiverName", ModuleNames.MESSAGE.value)

        userAuthData?.let {
            val chatRoomId = createChatRoomId(it.uid, receiverId)
            val data = MessageData(chatRoomId, it.uid, it.displayName ?: "", message.value, Timestamp(Date(System.currentTimeMillis())), arrayListOf(it.uid, receiverId))

            Logger.d(TAG, "sendMessage", "data: $data", ModuleNames.MESSAGE.value)
            FirebaseFirestoreHelper.getInstance().sendMessage(data) { task ->
                Logger.i(TAG, "sendMessage", "task status: ${task.isSuccessful}", ModuleNames.MESSAGE.value)

                if (task.isSuccessful) {

                    viewModelScope.launch(Dispatchers.IO) {
                        setChatRoomData(context, receiverId, receiverName, message.value)
                    }
                }
                messageState.value = MessageState.SendMessageStatus(task.isSuccessful)
            }
        } ?: run {
            messageState.value = MessageState.SendMessageStatus(false)
            Logger.e(TAG, "sendMessage", "user auth data is null", ModuleNames.MESSAGE.value)
        }
    }

    private suspend fun setChatRoomData(context: Context, receiverId: String, receiverName: String, message: String?) {
        Logger.d(TAG, "setChatRoomData", "receiverId: $receiverId receiverName: $receiverName", ModuleNames.MESSAGE.value)

        userAuthData?.let {
            val chatRoomId = createChatRoomId(it.uid, receiverId)
            val data = ChatRoomData(chatRoomId, it.uid, it.displayName ?: "", message, Timestamp(Date(System.currentTimeMillis())), arrayListOf(it.uid, receiverId), hashMapOf(Pair(it.uid, true), Pair(receiverId, false)))

            Logger.d(TAG, "setChatRoomData", "data: $data", ModuleNames.MESSAGE.value)
            FirebaseFirestoreHelper.getInstance().setChatRoomData(data) { task ->
                Logger.i(TAG, "setChatRoomData", "task status: ${task.isSuccessful}", ModuleNames.MESSAGE.value)
                messageState.value = MessageState.SetChatRoomDataStatus(task.isSuccessful)

                if (task.isSuccessful) {

                    viewModelScope.launch(Dispatchers.IO) {
                        sendNotification(context, receiverId, message)
                    }
                }
            }
        } ?: run {
            Logger.e(TAG, "setChatRoomData", "user auth data is null", ModuleNames.MESSAGE.value)
            messageState.value = MessageState.SetChatRoomDataStatus(false)
        }
    }

    private suspend fun sendNotification(context: Context, receiverId: String, message: String?) {
        Logger.d(TAG, "sendNotification", moduleName = ModuleNames.MESSAGE.value)

        userAuthData?.let {

            FirebaseFirestoreHelper.getInstance().getFCMToken(receiverId) { task ->
                Logger.i(TAG, "sendNotification", "task status: ${task.isSuccessful}", ModuleNames.MESSAGE.value)

                if (task.isSuccessful.not()) {
                    messageState.value = MessageState.SendNotificationStatus(false)
                }

                task.addOnSuccessListener { documentSnap ->
                    Logger.i(TAG, "sendNotification", "documentSnap size: ${documentSnap?.data?.size}", ModuleNames.MESSAGE.value)

                    if (documentSnap?.data != null) {
                        val token = documentSnap.data?.getValue(FCMTokenDataFields.FCM_TOKEN_ID.fieldName) as? String

                        Logger.d(TAG, "sendNotification", "token: $token", ModuleNames.MESSAGE.value)
                        val fcmMessageData = FCMPushNotificationRequestData(FCMMessageData(token, FCMNotificationData(it.displayName, message ?: "")))

                        Logger.d(TAG, "sendNotification", "fcmMessageData: $fcmMessageData", ModuleNames.MESSAGE.value)
                        val gsonDataTree = Gson()
                            .getAdapter(FCMPushNotificationRequestData::class.java)
                            .toJsonTree(fcmMessageData)
                            .toString()

                        viewModelScope.launch(Dispatchers.IO) {
                            FCMNotificationSender.getInstance().sendNotification(context, gsonDataTree, responseCb)
                        }
                    }
                }
            }
        } ?: run {
            Logger.e(TAG, "sendNotification", "user auth data is null", ModuleNames.MESSAGE.value)
            messageState.value = MessageState.SendNotificationStatus(false)
        }
    }

    fun getMessageItemViewType(messageData: MessageData?): Int {
        Logger.d(TAG, "getMessageItemViewType", "messageData: $messageData", ModuleNames.MESSAGE.value)

        return userAuthData?.let {
            return if (messageData?.senderId == it.uid) {
                MessageViewHolders.SELF.value
            } else {
                MessageViewHolders.OTHER.value
            }
        } ?: run {
            messageState.value = MessageState.FailedToLoadUserInformation("User auth information not available.")
            Int.MIN_VALUE
        }
    }

    fun deInit() {
        Logger.d(TAG, "deInit", moduleName = ModuleNames.MESSAGE.value)

        message.removeObserver(messageObserver)
        messageIntent.close()
    }

    companion object {
        private const val TAG = "MessageViewModel"
    }
}