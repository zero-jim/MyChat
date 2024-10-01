package com.exoleviathan.mychat.auth.models.emailverification

sealed class EmailVerificationState {
    data object Initial : EmailVerificationState()
    data class UserEmailVerificationStatus(val isVerified: Boolean) : EmailVerificationState()
    data class SendEmailVerificationStatus(val isEmailSend: Boolean) : EmailVerificationState()
}