package com.exoleviathan.mychat.message.model

import android.content.Context

sealed class MessageIntent {
    data class AddMessageUpdateListener(val receiverId: String) : MessageIntent()
    data object RemoveMessageUpdateListener : MessageIntent()
    data class SendMessage(val context: Context, val receiverId: String, val receiverName: String) : MessageIntent()
}