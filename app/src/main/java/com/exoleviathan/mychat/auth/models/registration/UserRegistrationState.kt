package com.exoleviathan.mychat.auth.models.registration

sealed class UserRegistrationState {
    data object Initial : UserRegistrationState()
    data class UserRegistrationStatus(val data: UserRegistrationData) : UserRegistrationState()
}