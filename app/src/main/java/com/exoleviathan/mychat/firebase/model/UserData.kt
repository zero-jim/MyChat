package com.exoleviathan.mychat.firebase.model

data class UserData(
    val uid: String,
    val displayName: String?,
    val photoUrl: String?,
    val email: String?,
    val isEmailVerified: Boolean?,
    val status: String? = "Hello there! I am using My Chat application.",
    val creationTime: String? = "",
    val friends: ArrayList<String?> = arrayListOf(),
    val groups: ArrayList<String?> = arrayListOf()
)