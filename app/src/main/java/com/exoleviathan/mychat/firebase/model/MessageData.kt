package com.exoleviathan.mychat.firebase.model

import com.google.firebase.Timestamp

data class MessageData(
    val message: String?,
    val messageType: Int,
    val timeStamp: Timestamp?
)