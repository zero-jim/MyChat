package com.exoleviathan.mychat.firebase.model

import com.google.firebase.Timestamp

data class ChatRoomData(
    val participantsId: ArrayList<*>?,
    val lastMessage: String?,
    val lastMessageSender: String?,
    val timestamp: Timestamp?,
    val participantsName: ArrayList<*>? = null,
    val readStatus: Boolean = false
)