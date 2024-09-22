package com.exoleviathan.mychat.auth.models

enum class RegistrationFieldValidators(val validator: IAuthEditTextFieldValidator) {
    USERNAME(UserNameFieldValidator(ValidationMessages.REGISTRATION_SUCCESSFUL)),
    EMAIL(EmailFieldValidator(ValidationMessages.REGISTRATION_SUCCESSFUL)),
    PASSWORD(PasswordFieldValidator(ValidationMessages.REGISTRATION_SUCCESSFUL))
}