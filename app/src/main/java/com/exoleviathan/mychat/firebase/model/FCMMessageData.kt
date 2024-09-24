package com.exoleviathan.mychat.firebase.model

data class FCMMessageData(
    val token: String?,
    val notification: FCMNotificationData?
)