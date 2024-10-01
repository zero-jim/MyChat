package com.exoleviathan.mychat.auth.models.forgotpassword

data class ForgotPasswordData(
    val isEmailSendSuccess: Boolean,
    val emailSendMsg: String?,
    val emailSendMsgDetails: String?
)