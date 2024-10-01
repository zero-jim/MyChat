package com.exoleviathan.mychat.firebase.firestore.fields

enum class FirestoreCollections(val collectionName: String) {
    USER_PROFILE("user_profile"),
    FCM_TOKEN("fcm_token"),
    CHAT_ROOMS("chat_rooms"),
    MESSAGES("messages")
}