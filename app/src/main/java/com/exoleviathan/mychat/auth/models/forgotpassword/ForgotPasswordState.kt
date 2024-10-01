package com.exoleviathan.mychat.auth.models.forgotpassword

sealed class ForgotPasswordState {
    data object Initial : ForgotPasswordState()
    data class ForgotPasswordEmailSendStatus(val data: ForgotPasswordData) : ForgotPasswordState()
}