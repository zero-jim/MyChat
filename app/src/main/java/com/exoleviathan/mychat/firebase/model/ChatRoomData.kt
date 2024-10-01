package com.exoleviathan.mychat.firebase.model

import com.google.firebase.Timestamp
import java.util.Date

data class ChatRoomData(
    val chatRoomId: String = "",
    val lastMessageSenderId: String = "",
    val lastMessageSenderName: String = "",
    val lastMessage: String? = "",
    val timestamp: Timestamp = Timestamp(Date(System.currentTimeMillis())),
    val participantIdList: ArrayList<String?> = arrayListOf(),
    val readStatus: HashMap<String?, Boolean> = hashMapOf()
)