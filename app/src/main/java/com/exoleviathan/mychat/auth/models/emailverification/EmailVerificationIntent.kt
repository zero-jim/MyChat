package com.exoleviathan.mychat.auth.models.emailverification

sealed class EmailVerificationIntent {
    data object SendUserEmailVerification : EmailVerificationIntent()
    data object CheckIfEmailVerified : EmailVerificationIntent()
}