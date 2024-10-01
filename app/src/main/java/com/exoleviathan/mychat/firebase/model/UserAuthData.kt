package com.exoleviathan.mychat.firebase.model

data class UserAuthData(
    val uid: String = "",
    val email: String? = "",
    val displayName: String? = "",
    val isEmailVerified: Boolean = false,
    val photoUrl: String? = "",
    val status: String? = "Hello there! I am using My Chat application.",
    val creationTime: String? = "",
    val friendList: ArrayList<String> = arrayListOf(),
    val groupList: ArrayList<String> = arrayListOf()
)