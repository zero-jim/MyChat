package com.exoleviathan.mychat.message.model

import com.exoleviathan.mychat.firebase.model.MessageData

sealed class MessageState {
    data object Initial : MessageState()
    data class IsSendButtonEnabled(val isEnabled: Boolean) : MessageState()
    data class FailedToLoadUserInformation(val message: String?) : MessageState()
    data class MessageListUpdated(val messageList: List<MessageData?>) : MessageState()
    data class SendMessageStatus(val isSuccessful: Boolean) : MessageState()
    data class SetChatRoomDataStatus(val isSuccessful: Boolean) : MessageState()
    data class SendNotificationStatus(val isSuccessful: Boolean) : MessageState()
}