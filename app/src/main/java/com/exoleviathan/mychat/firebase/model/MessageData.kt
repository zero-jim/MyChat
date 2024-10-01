package com.exoleviathan.mychat.firebase.model

import com.google.firebase.Timestamp
import java.util.Date

data class MessageData(
    val chatRoomId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val message: String? = "",
    val timeStamp: Timestamp = Timestamp(Date(System.currentTimeMillis())),
    val receiverList: ArrayList<String?> = arrayListOf()
)