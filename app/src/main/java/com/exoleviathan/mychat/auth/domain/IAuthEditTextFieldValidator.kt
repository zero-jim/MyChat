package com.exoleviathan.mychat.auth.domain

import com.exoleviathan.mychat.auth.models.ValidationMessages

interface IAuthEditTextFieldValidator {
    fun <T> validate(field: T?): ValidationMessages
}