package com.exoleviathan.mychat.auth.models.login

sealed class UserLoginIntent {
    data object LoginUserWithEmailAndPassword : UserLoginIntent()
    data object CheckIfUserEmailVerified : UserLoginIntent()
}