package com.exoleviathan.mychat.auth.domain

import com.exoleviathan.mychat.auth.domain.validator.EmailFieldValidator
import com.exoleviathan.mychat.auth.domain.validator.PasswordFieldValidator
import com.exoleviathan.mychat.auth.domain.validator.UserNameFieldValidator
import com.exoleviathan.mychat.auth.models.ValidationMessages

enum class RegistrationFieldValidators(val validator: IAuthEditTextFieldValidator) {
    USERNAME(UserNameFieldValidator(ValidationMessages.REGISTRATION_SUCCESSFUL)),
    EMAIL(EmailFieldValidator(ValidationMessages.REGISTRATION_SUCCESSFUL)),
    PASSWORD(PasswordFieldValidator(ValidationMessages.REGISTRATION_SUCCESSFUL))
}