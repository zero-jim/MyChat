package com.exoleviathan.mychat.auth.models.registration

data class UserRegistrationData(
    val isSuccessful: Boolean,
    val registrationMessage: String?,
    val registrationMessageDetails: String?
)