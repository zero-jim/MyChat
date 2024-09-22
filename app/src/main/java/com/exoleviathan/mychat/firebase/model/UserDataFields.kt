package com.exoleviathan.mychat.firebase.model

enum class UserDataFields(val fieldName: String) {
    USER_ID("user_id"),
    USER_NAME("user_name"),
    PROFILE_PHOTO_URL("profile_photo_url"),
    EMAIL("email_id"),
    STATUS("status"),
    CREATION_TIME("creation_time"),
    FRIEND_LIST("friend_list"),
    GROUP_LIST("group_list")
}