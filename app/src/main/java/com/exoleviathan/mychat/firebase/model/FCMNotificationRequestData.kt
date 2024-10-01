package com.exoleviathan.mychat.firebase.model

data class FCMPushNotificationRequestData(
    val message: FCMMessageData?
)

data class FCMMessageData(
    val token: String?,
    val notification: FCMNotificationData?
)

data class FCMNotificationData(
    private val title: String?,
    private val body: String?
)