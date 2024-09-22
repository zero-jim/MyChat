package com.exoleviathan.mychat.home.viewmodel

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.exoleviathan.mychat.firebase.auth.FirebaseAuthenticationHelper
import com.exoleviathan.mychat.firebase.firestore.FirebaseFirestoreHelper
import com.exoleviathan.mychat.firebase.model.ChatRoomData
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames

class HomeViewModel : ViewModel() {
    private val currentUserId = FirebaseAuthenticationHelper.getInstance()?.getUserInformation()?.uid
    private val currentUserName = FirebaseAuthenticationHelper.getInstance()?.getUserInformation()?.displayName
    private val _ongoingConversations = MutableLiveData<ArrayList<ChatRoomData>>()

    val ongoingConversations: LiveData<ArrayList<ChatRoomData>> = _ongoingConversations

    fun addOngoingConversationListener() {
        Logger.d(TAG, "addOngoingConversationListener", moduleName = ModuleNames.HOME.value)

        currentUserId?.let {
            FirebaseFirestoreHelper.getInstance()?.addChatRoomListener(currentUserId) {
                Logger.i(TAG, "addOngoingConversationListener", "conversationSize: ${it.size}", ModuleNames.HOME.value)
                _ongoingConversations.postValue(it)
            }
        }
    }

    fun removeOngoingConversationListener() {
        currentUserId?.let {
            FirebaseFirestoreHelper.getInstance()?.removeChatRoomListener(it)
        }
    }

    fun getConversationUserId(participants: ArrayList<*>?): String? {
        participants?.remove(currentUserId)

        return participants?.let {
            return if (participants.size > 0) {
                participants[0] as? String
            } else {
                null
            }
        }
    }

    fun getConversationUserName(participants: ArrayList<*>?): String? {
        participants?.remove(currentUserName)

        return participants?.let {
            return if (participants.size > 0) {
                participants[0] as? String
            } else {
                null
            }
        }
    }

    fun getMessageSender(lastMessageSender: String?): CharSequence? {
        return if (TextUtils.equals(lastMessageSender, currentUserName)) {
            "Me"
        } else {
            lastMessageSender
        }
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}