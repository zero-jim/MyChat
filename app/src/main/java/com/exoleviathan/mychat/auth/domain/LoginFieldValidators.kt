package com.exoleviathan.mychat.auth.domain

import com.exoleviathan.mychat.auth.domain.validator.EmailFieldValidator
import com.exoleviathan.mychat.auth.domain.validator.PasswordFieldValidator
import com.exoleviathan.mychat.auth.models.ValidationMessages

enum class LoginFieldValidators(val validator: IAuthEditTextFieldValidator) {
    EMAIL(EmailFieldValidator(ValidationMessages.LOGIN_SUCCESSFUL)),
    PASSWORD(PasswordFieldValidator(ValidationMessages.LOGIN_SUCCESSFUL))
}