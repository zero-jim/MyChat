package com.exoleviathan.mychat.auth.models.registration

sealed class UserRegistrationIntent {
    data object RegisterNewUserWithEmailAndPassword : UserRegistrationIntent()
}