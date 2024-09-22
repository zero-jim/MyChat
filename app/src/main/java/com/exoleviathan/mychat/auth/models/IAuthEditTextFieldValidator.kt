package com.exoleviathan.mychat.auth.models

interface IAuthEditTextFieldValidator {
    fun <T> validate(field: T?): ValidationMessages
}