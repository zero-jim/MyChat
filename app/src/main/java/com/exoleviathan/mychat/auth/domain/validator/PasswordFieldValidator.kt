package com.exoleviathan.mychat.auth.domain.validator

import com.exoleviathan.mychat.auth.domain.IAuthEditTextFieldValidator
import com.exoleviathan.mychat.auth.models.ValidationMessages
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import java.util.regex.Pattern

class PasswordFieldValidator(private val validationMessage: ValidationMessages) : IAuthEditTextFieldValidator {

    override fun <T> validate(field: T?): ValidationMessages {
        Logger.d(TAG, "validate", "password: $field", ModuleNames.AUTH.value)

        val password = field as? String
        return if (password.isNullOrEmpty()) {
            ValidationMessages.INVALID_PASSWORD
        } else {
            if (Pattern.compile(PASSWORD_REGEX_PATTERN).matcher(password).matches()) {
                validationMessage
            } else {
                ValidationMessages.INVALID_PASSWORD
            }
        }
    }

    companion object {
        private const val TAG = "PasswordFieldValidator"
        private const val PASSWORD_REGEX_PATTERN = "^" +
                "(?=.*[0-9])" +         // at least 1 digit
                "(?=.*[a-z])" +         // at least 1 lower case letter
                "(?=.*[A-Z])" +         // at least 1 upper case letter
                "(?=.*[a-zA-Z])" +      // any letter
                "(?=.*[@#$%^&+=])" +    // at least 1 special character
                "(?=\\S+$)" +           // no white spaces
                ".{8,}" +               // at least 8 characters
                "$"
    }
}