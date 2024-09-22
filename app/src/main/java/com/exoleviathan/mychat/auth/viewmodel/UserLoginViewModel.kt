package com.exoleviathan.mychat.auth.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.exoleviathan.mychat.auth.callbacks.IAuthActionCallback
import com.exoleviathan.mychat.auth.models.LoginFieldValidators
import com.exoleviathan.mychat.auth.models.ValidationMessages
import com.exoleviathan.mychat.firebase.auth.FirebaseAuthenticationHelper
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames

class UserLoginViewModel : ViewModel() {
    val emailText = MutableLiveData<String?>(null)
    val passwordText = MutableLiveData<String?>(null)

    private fun validateEditTextFields(): ValidationMessages {
        Logger.d(TAG, "validateEditTextFields", moduleName = ModuleNames.AUTH.value)
        var validationMessage: ValidationMessages

        LoginFieldValidators.entries.forEach {
            validationMessage = when (it) {
                LoginFieldValidators.EMAIL -> {
                    it.validator.validate(emailText.value)
                }

                LoginFieldValidators.PASSWORD -> {
                    it.validator.validate(passwordText.value)
                }
            }

            if (validationMessage != ValidationMessages.LOGIN_SUCCESSFUL) {
                Logger.e(TAG, "validateEditTextFields", "validationMessage: $validationMessage", ModuleNames.AUTH.value)
                return validationMessage
            }
        }

        return ValidationMessages.LOGIN_SUCCESSFUL
    }

    fun signInButtonAction(callback: IAuthActionCallback?) {
        Logger.i(TAG, "signInButtonAction", moduleName = ModuleNames.AUTH.value)

        val validationMessage = validateEditTextFields()
        if (validationMessage != ValidationMessages.LOGIN_SUCCESSFUL) {
            Logger.e(TAG, "signInButtonAction", "validationMessage: $validationMessage", ModuleNames.AUTH.value)

            callback?.onError(validationMessage.msg, validationMessage.msgDetails)
            return
        }

        FirebaseAuthenticationHelper.getInstance()
            ?.signInWithEmailAndPassword(emailText.value ?: "", passwordText.value ?: "") { task ->
                if (task.isSuccessful) {
                    Logger.i(TAG, "signInButtonAction", "sign in task is completed", ModuleNames.AUTH.value)
                    callback?.onSuccess()
                } else {
                    Logger.e(TAG, "signInButtonAction", "error task: $task exception: ${task.exception?.message}", ModuleNames.AUTH.value)
                    callback?.onError(ValidationMessages.LOGIN_ERROR.msg, task.exception?.message ?: ValidationMessages.LOGIN_ERROR.msgDetails)
                }
            }
    }

    fun isUserEmailVerified(): Boolean {
        Logger.i(TAG, "isUserEmailVerified", moduleName = ModuleNames.AUTH.value)
        return FirebaseAuthenticationHelper.getInstance()
            ?.isUserEmailVerified() == true
    }

    fun sendUserEmailVerification() {
        Logger.i(TAG, "isUserEmailVerified", moduleName = ModuleNames.AUTH.value)
        FirebaseAuthenticationHelper.getInstance()
            ?.sendEmailVerification()
    }

    companion object {
        private const val TAG = "UserLoginViewModel"
    }
}