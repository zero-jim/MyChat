package com.exoleviathan.mychat.auth.models.login

sealed class UserLoginState {
    data object Initial : UserLoginState()
    data class UserLoginStatus(val status: UserLoginData) : UserLoginState()
    data class UserEmailVerificationStatus(val isVerified: Boolean) : UserLoginState()
}