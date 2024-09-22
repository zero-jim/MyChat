package com.exoleviathan.mychat.auth.models

import android.util.Patterns
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames

class EmailFieldValidator(private val validationMessage: ValidationMessages) : IAuthEditTextFieldValidator {

    override fun <T> validate(field: T?): ValidationMessages {
        Logger.d(TAG, "validate", "email: $field", ModuleNames.AUTH.value)

        val email = field as? String
        return if (email.isNullOrEmpty()) {
            ValidationMessages.INVALID_EMAIL_ADDRESS
        } else {
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                validationMessage
            } else {
                ValidationMessages.INVALID_EMAIL_ADDRESS
            }
        }
    }

    companion object {
        private const val TAG = "EmailFieldValidator"
    }
}