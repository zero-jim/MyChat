package com.exoleviathan.mychat.auth.viewmodel

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exoleviathan.mychat.auth.callbacks.IAuthActionCallback
import com.exoleviathan.mychat.auth.models.RegistrationFieldValidators
import com.exoleviathan.mychat.auth.models.ValidationMessages
import com.exoleviathan.mychat.firebase.auth.FirebaseAuthenticationHelper
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class UserRegistrationViewModel : ViewModel() {
    var userNameText = MutableLiveData<String?>(null)
    var emailText = MutableLiveData<String?>(null)
    var passwordText = MutableLiveData<String?>(null)
    var confirmPasswordText = MutableLiveData<String?>(null)

    private fun validateEditTextFields(): ValidationMessages {
        Logger.d(TAG, "validateEditTextFields", moduleName = ModuleNames.AUTH.value)
        var validationMessage: ValidationMessages

        RegistrationFieldValidators.entries.forEach {
            validationMessage = when (it) {
                RegistrationFieldValidators.USERNAME -> {
                    it.validator.validate(userNameText.value)
                }

                RegistrationFieldValidators.EMAIL -> {
                    it.validator.validate(emailText.value)
                }

                RegistrationFieldValidators.PASSWORD -> {
                    it.validator.validate(passwordText.value)
                }
            }

            if (validationMessage != ValidationMessages.REGISTRATION_SUCCESSFUL) {
                Logger.e(TAG, "validateEditTextFields", "validationMessage: $validationMessage", ModuleNames.AUTH.value)
                return validationMessage
            }
        }

        if (TextUtils.equals(passwordText.value, confirmPasswordText.value).not()) {
            validationMessage = ValidationMessages.PASSWORD_MISMATCHED
            Logger.e(TAG, "validateEditTextFields", "validationMessage: $validationMessage", ModuleNames.AUTH.value)
            return validationMessage
        }

        return ValidationMessages.REGISTRATION_SUCCESSFUL
    }

    fun registerButtonAction(callback: IAuthActionCallback?) {
        Logger.d(TAG, "registerButtonAction", moduleName = ModuleNames.AUTH.value)

        val validationMessage = validateEditTextFields()
        if (validationMessage != ValidationMessages.REGISTRATION_SUCCESSFUL) {
            Logger.e(TAG, "registerButtonAction", "validationMessage: $validationMessage", ModuleNames.AUTH.value)

            callback?.onError(validationMessage.msg, validationMessage.msgDetails)
            return
        }

        FirebaseAuthenticationHelper.getInstance()
            ?.createUserWithEmailAndPassword(emailText.value ?: "", passwordText.value ?: "") { task ->

                task.addOnSuccessListener {
                    CoroutineScope(viewModelScope.coroutineContext).launch {
                        Logger.i(TAG, "registerButtonAction", "updating profile with display name", ModuleNames.AUTH.value)

                        val profileUpdates = userProfileChangeRequest {
                            displayName = userNameText.value
                        }

                        it?.user?.updateProfile(profileUpdates)?.addOnSuccessListener {
                            Logger.i(TAG, "registerButtonAction", "profile update is completed", ModuleNames.AUTH.value)
                        }
                    }
                }

                if (task.isSuccessful) {
                    Logger.i(TAG, "registerButtonAction", "user registration is completed", ModuleNames.AUTH.value)
                    callback?.onSuccess()
                } else {
                    Logger.e(TAG, "registerButtonAction", "error task: $task exception: ${task.exception?.message}", ModuleNames.AUTH.value)
                    callback?.onError(ValidationMessages.REGISTRATION_ERROR.msg, task.exception?.message ?: ValidationMessages.REGISTRATION_ERROR.msgDetails)
                }
            }
    }

    fun sendUserEmailVerification() {
        Logger.i(TAG, "isUserEmailVerified", moduleName = ModuleNames.AUTH.value)
        FirebaseAuthenticationHelper.getInstance()
            ?.sendEmailVerification()
    }

    companion object {
        private const val TAG = "UserRegistrationViewModel"
    }
}