package com.exoleviathan.mychat.auth.domain.validator

import com.exoleviathan.mychat.auth.domain.IAuthEditTextFieldValidator
import com.exoleviathan.mychat.auth.models.ValidationMessages
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import java.util.regex.Pattern

class UserNameFieldValidator(private val validationMessage: ValidationMessages) : IAuthEditTextFieldValidator {

    override fun <T> validate(field: T?): ValidationMessages {
        Logger.d(TAG, "validate", "userName: $field", ModuleNames.AUTH.value)

        val userName = field as? String
        return if (userName.isNullOrEmpty()) {
            ValidationMessages.INVALID_USERNAME
        } else {
            if (Pattern.compile(USERNAME_REGEX_PATTERN).matcher(userName).matches()) {
                validationMessage
            } else {
                ValidationMessages.INVALID_USERNAME
            }
        }
    }

    companion object {
        private const val TAG = "UserNameFieldValidator"
        private const val USERNAME_REGEX_PATTERN = "^" +
                "(?=.*[a-zA-Z0-9])" +   // any letter
                "(?=.*[ _.-])" +        // can only contain [' ' or '_' or '.' or '-']
                ".{3,21}" +             // at least 3 characters and at most 21 character
                "$"
    }
}