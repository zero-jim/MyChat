package com.exoleviathan.mychat.auth.ui.registration

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exoleviathan.mychat.auth.domain.RegistrationFieldValidators
import com.exoleviathan.mychat.auth.models.ValidationMessages
import com.exoleviathan.mychat.auth.models.registration.UserRegistrationData
import com.exoleviathan.mychat.auth.models.registration.UserRegistrationIntent
import com.exoleviathan.mychat.auth.models.registration.UserRegistrationState
import com.exoleviathan.mychat.firebase.auth.FirebaseAuthHelper
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class UserRegistrationViewModel : ViewModel() {
    var userNameText = MutableLiveData<String?>(null)
    var emailText = MutableLiveData<String?>(null)
    var passwordText = MutableLiveData<String?>(null)
    var confirmPasswordText = MutableLiveData<String?>(null)

    val userRegistrationIntent = Channel<UserRegistrationIntent>(Channel.UNLIMITED)
    val userRegistrationState = MutableStateFlow<UserRegistrationState>(UserRegistrationState.Initial)

    init {
        Logger.d(TAG, "init", moduleName = ModuleNames.AUTH.value)

        viewModelScope.launch {

            userRegistrationIntent.consumeAsFlow().collect { intent ->
                Logger.i(TAG, "init", "intent: $intent", ModuleNames.AUTH.value)

                when (intent) {
                    UserRegistrationIntent.RegisterNewUserWithEmailAndPassword -> {
                        viewModelScope.launch(Dispatchers.IO) {
                            registerNewUserWithEmailAndPassword()
                        }
                    }
                }
            }
        }
    }

    private fun validateUserRegistrationEditTextFields(): ValidationMessages {
        Logger.d(TAG, "validateUserRegistrationEditTextFields", moduleName = ModuleNames.AUTH.value)

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
                Logger.e(TAG, "validateUserRegistrationEditTextFields", "validationMessage: $validationMessage", ModuleNames.AUTH.value)
                return validationMessage
            }
        }

        if (TextUtils.equals(passwordText.value, confirmPasswordText.value).not()) {
            validationMessage = ValidationMessages.PASSWORD_MISMATCHED
            Logger.e(TAG, "validateUserRegistrationEditTextFields", "validationMessage: $validationMessage", ModuleNames.AUTH.value)
            return validationMessage
        }

        return ValidationMessages.REGISTRATION_SUCCESSFUL
    }

    private suspend fun updateUserProfileInformation(profileUpdates: UserProfileChangeRequest) {
        Logger.d(TAG, "updateUserProfileInformation", "profileUpdates: $profileUpdates", ModuleNames.AUTH.value)

        FirebaseAuthHelper.getInstance().updateUserProfileInformation(profileUpdates) {
            if (it.isSuccessful) {
                Logger.i(TAG, "updateUserProfileInformation", "profile update is completed", ModuleNames.AUTH.value)
            } else {
                Logger.e(TAG, "updateUserProfileInformation", "failed to update user profile", ModuleNames.AUTH.value)
            }
        }
    }

    private suspend fun registerNewUserWithEmailAndPassword() {
        Logger.d(TAG, "registerNewUserWithEmailAndPassword", moduleName = ModuleNames.AUTH.value)

        val validationMessage = validateUserRegistrationEditTextFields()
        if (validationMessage != ValidationMessages.REGISTRATION_SUCCESSFUL) {
            Logger.e(TAG, "registerNewUserWithEmailAndPassword", "validationMessage: $validationMessage", ModuleNames.AUTH.value)

            val data = UserRegistrationData(false, validationMessage.msg, validationMessage.msgDetails)
            userRegistrationState.emit(UserRegistrationState.UserRegistrationStatus(data))
            return
        }

        FirebaseAuthHelper.getInstance().createNewUserWithEmailAndPassword(emailText.value ?: "", passwordText.value ?: "") { task ->
            task.addOnSuccessListener {
                viewModelScope.launch(Dispatchers.IO) {
                    val profileUpdates = userProfileChangeRequest {
                        displayName = userNameText.value
                    }
                    updateUserProfileInformation(profileUpdates)
                }
            }

            if (task.isSuccessful) {
                Logger.i(TAG, "registerNewUserWithEmailAndPassword", "user registration is completed", ModuleNames.AUTH.value)

                val data = UserRegistrationData(true, validationMessage.msg, validationMessage.msgDetails)
                userRegistrationState.value = UserRegistrationState.UserRegistrationStatus(data)
            } else {
                Logger.e(TAG, "registerNewUserWithEmailAndPassword", "error task: $task exception: ${task.exception?.message}", ModuleNames.AUTH.value)

                val data = UserRegistrationData(false, ValidationMessages.REGISTRATION_ERROR.msg, task.exception?.message ?: ValidationMessages.REGISTRATION_ERROR.msgDetails)
                userRegistrationState.value = UserRegistrationState.UserRegistrationStatus(data)
            }
        }
    }

    companion object {
        private const val TAG = "UserRegistrationViewModel"
    }
}