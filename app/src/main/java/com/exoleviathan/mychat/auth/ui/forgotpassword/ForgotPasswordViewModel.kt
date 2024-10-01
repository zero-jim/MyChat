package com.exoleviathan.mychat.auth.ui.forgotpassword

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exoleviathan.mychat.auth.domain.validator.EmailFieldValidator
import com.exoleviathan.mychat.auth.models.ValidationMessages
import com.exoleviathan.mychat.auth.models.forgotpassword.ForgotPasswordData
import com.exoleviathan.mychat.auth.models.forgotpassword.ForgotPasswordIntent
import com.exoleviathan.mychat.auth.models.forgotpassword.ForgotPasswordState
import com.exoleviathan.mychat.firebase.auth.FirebaseAuthHelper
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel : ViewModel() {
    val emailText = MutableLiveData<String?>(null)

    val forgotPasswordIntent = Channel<ForgotPasswordIntent>(Channel.UNLIMITED)
    val forgotPasswordState = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Initial)

    init {
        Logger.d(TAG, "init", moduleName = ModuleNames.AUTH.value)

        viewModelScope.launch {
            forgotPasswordIntent.consumeAsFlow().collect { intent ->
                Logger.i(TAG, "init", "intent: $intent", ModuleNames.AUTH.value)

                when (intent) {
                    ForgotPasswordIntent.SendResetPasswordEmail -> {
                        viewModelScope.launch(Dispatchers.IO) {
                            sendResetPasswordEmail()
                        }
                    }
                }
            }
        }
    }

    private suspend fun sendResetPasswordEmail() {
        Logger.d(TAG, "sendResetPasswordEmail", moduleName = ModuleNames.AUTH.value)

        val validationMessages = EmailFieldValidator(ValidationMessages.EMAIL_SEND_SUCCESSFUL).validate(emailText.value)
        if (validationMessages != ValidationMessages.EMAIL_SEND_SUCCESSFUL) {
            Logger.e(TAG, "sendResetPasswordEmail", "email is not correct", ModuleNames.AUTH.value)

            val data = ForgotPasswordData(false, validationMessages.msg, validationMessages.msgDetails)
            forgotPasswordState.emit(ForgotPasswordState.ForgotPasswordEmailSendStatus(data))
            return
        }

        FirebaseAuthHelper.getInstance().sendPasswordResetEmail(emailText.value ?: "") { task ->
            Logger.i(TAG, "sendResetPasswordEmail", "isSuccessful: ${task.isSuccessful}", ModuleNames.AUTH.value)

            if (task.isSuccessful) {
                val data = ForgotPasswordData(true, validationMessages.msg, validationMessages.msgDetails)
                forgotPasswordState.value = ForgotPasswordState.ForgotPasswordEmailSendStatus(data)
            } else {
                val data = ForgotPasswordData(false, ValidationMessages.SEND_EMAIL_ERROR.msg, task.exception?.message ?: ValidationMessages.SEND_EMAIL_ERROR.msgDetails)
                forgotPasswordState.value = ForgotPasswordState.ForgotPasswordEmailSendStatus(data)
            }
        }
    }

    companion object {
        private const val TAG = "ForgotPasswordViewModel"
    }
}