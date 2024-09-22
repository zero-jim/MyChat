package com.exoleviathan.mychat.firebase.model

enum class FirestoreCollections(val collectionName: String) {
    USER_PROFILE("user_profile"),
    CHAT_ROOMS("chat_rooms"),
    MESSAGES("messages")
}