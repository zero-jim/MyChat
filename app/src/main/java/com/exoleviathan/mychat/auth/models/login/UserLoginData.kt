package com.exoleviathan.mychat.auth.models.login

data class UserLoginData(
    val isLoginSuccessful: Boolean,
    val loginResponseMessage: String?,
    val loginResponseMessageDetails: String?
)