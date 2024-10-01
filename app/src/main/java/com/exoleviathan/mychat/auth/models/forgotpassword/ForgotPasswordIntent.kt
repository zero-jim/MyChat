package com.exoleviathan.mychat.auth.models.forgotpassword

sealed class ForgotPasswordIntent {
    data object SendResetPasswordEmail : ForgotPasswordIntent()
}