package com.exoleviathan.mychat.auth.ui.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exoleviathan.mychat.auth.domain.LoginFieldValidators
import com.exoleviathan.mychat.auth.models.ValidationMessages
import com.exoleviathan.mychat.auth.models.login.UserLoginIntent
import com.exoleviathan.mychat.auth.models.login.UserLoginData
import com.exoleviathan.mychat.auth.models.login.UserLoginState
import com.exoleviathan.mychat.firebase.auth.FirebaseAuthHelper
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class UserLoginViewModel : ViewModel() {
    val emailText = MutableLiveData<String?>(null)
    val passwordText = MutableLiveData<String?>(null)

    val userLoginIntent = Channel<UserLoginIntent>(Channel.UNLIMITED)
    val userLoginState = MutableStateFlow<UserLoginState>(UserLoginState.Initial)

    init {
        Logger.d(TAG, "init", moduleName = ModuleNames.AUTH.value)

        viewModelScope.launch {

            userLoginIntent.consumeAsFlow().collect { intent ->
                Logger.i(TAG, "init", "intent: $intent", ModuleNames.AUTH.value)

                when (intent) {
                    UserLoginIntent.LoginUserWithEmailAndPassword -> {
                        viewModelScope.launch(Dispatchers.IO) {
                            signInUserWithEmailAndPassword()
                        }
                    }

                    UserLoginIntent.CheckIfUserEmailVerified -> {
                        viewModelScope.launch(Dispatchers.IO) {
                            isUserEmailVerified()
                        }
                    }
                }
            }
        }
    }

    private fun validateUserLoginTextFields(email: String?, password: String?): ValidationMessages {
        Logger.d(TAG, "validateUserLoginTextFields", "email: $email password: $password", ModuleNames.AUTH.value)

        var validationMessage: ValidationMessages

        LoginFieldValidators.entries.forEach {

            validationMessage = when (it) {
                LoginFieldValidators.EMAIL -> {
                    it.validator.validate(email)
                }

                LoginFieldValidators.PASSWORD -> {
                    it.validator.validate(password)
                }
            }

            if (validationMessage != ValidationMessages.LOGIN_SUCCESSFUL) {
                Logger.e(TAG, "validateUserLoginTextFields", "validationMessage: $validationMessage", ModuleNames.AUTH.value)
                return validationMessage
            }
        }

        return ValidationMessages.LOGIN_SUCCESSFUL
    }

    private suspend fun signInUserWithEmailAndPassword() {
        Logger.d(TAG, "signInUserWithEmailAndPassword", moduleName = ModuleNames.AUTH.value)

        val validationMessage = validateUserLoginTextFields(emailText.value ?: "", passwordText.value ?: "")
        if (validationMessage != ValidationMessages.LOGIN_SUCCESSFUL) {
            Logger.e(TAG, "signInUserWithEmailAndPassword", "validationMessage: $validationMessage", ModuleNames.AUTH.value)

            val data = UserLoginData(false, validationMessage.msg, validationMessage.msgDetails)
            this.userLoginState.emit(UserLoginState.UserLoginStatus(data))
            return
        }

        FirebaseAuthHelper.getInstance().signInUserWithEmailAndPassword(emailText.value ?: "", passwordText.value ?: "") { task ->
            if (task.isSuccessful) {
                Logger.i(TAG, "signInUserWithEmailAndPassword", "sign in task is completed", ModuleNames.AUTH.value)
                val data = UserLoginData(true, ValidationMessages.LOGIN_SUCCESSFUL.msg, ValidationMessages.LOGIN_SUCCESSFUL.msgDetails)
                this.userLoginState.value = UserLoginState.UserLoginStatus(data)
            } else {
                Logger.e(TAG, "signInUserWithEmailAndPassword", "error task: $task exception: ${task.exception?.message}", ModuleNames.AUTH.value)
                val data = UserLoginData(false, ValidationMessages.LOGIN_ERROR.msg, task.exception?.message ?: ValidationMessages.LOGIN_ERROR.msgDetails)
                this.userLoginState.value = UserLoginState.UserLoginStatus(data)
            }
        }
    }

    private suspend fun isUserEmailVerified() {
        Logger.d(TAG, "isUserEmailVerified", moduleName = ModuleNames.AUTH.value)

        FirebaseAuthHelper.getInstance().isUserEmailVerified { isVerified ->
            userLoginState.value = UserLoginState.UserEmailVerificationStatus(isVerified)
        }
    }

    companion object {
        private const val TAG = "UserLoginViewModel"
    }
}