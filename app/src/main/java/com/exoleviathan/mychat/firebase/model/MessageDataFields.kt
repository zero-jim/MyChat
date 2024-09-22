package com.exoleviathan.mychat.firebase.model

enum class MessageDataFields(val fieldName: String) {
    SENDER_ID("sender_id"),
    MESSAGE("msg"),
    TIMESTAMP("timestamp")
}