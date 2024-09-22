package com.exoleviathan.mychat.auth.models

enum class LoginFieldValidators(val validator: IAuthEditTextFieldValidator) {
    EMAIL(EmailFieldValidator(ValidationMessages.LOGIN_SUCCESSFUL)),
    PASSWORD(PasswordFieldValidator(ValidationMessages.LOGIN_SUCCESSFUL))
}