package com.exoleviathan.mychat.auth.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.exoleviathan.mychat.auth.callbacks.IAuthActionCallback
import com.exoleviathan.mychat.auth.models.EmailFieldValidator
import com.exoleviathan.mychat.auth.models.ValidationMessages
import com.exoleviathan.mychat.firebase.auth.FirebaseAuthenticationHelper
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames

class ForgotPasswordViewModel : ViewModel() {
    val emailText = MutableLiveData<String?>(null)

    fun sendResetPasswordEmail(callback: IAuthActionCallback?) {
        Logger.d(TAG, "sendResetPasswordEmail")

        val validationMessages = EmailFieldValidator(ValidationMessages.EMAIL_SEND_SUCCESS).validate(emailText.value)
        if (validationMessages != ValidationMessages.EMAIL_SEND_SUCCESS) {
            Logger.e(TAG, "sendResetPasswordEmail", "email is not correct", ModuleNames.AUTH.value)
            callback?.onError(validationMessages.msg, validationMessages.msgDetails)
            return
        }

        emailText.value?.let {
            FirebaseAuthenticationHelper.getInstance()
                ?.sendPasswordResetEmail(it) { task ->
                    if (task.isSuccessful) {
                        callback?.onSuccess()
                    } else {
                        callback?.onError(ValidationMessages.SEND_EMAIL_ERROR.msg, task.exception?.message ?: ValidationMessages.SEND_EMAIL_ERROR.msgDetails)
                    }
                }
        }
    }

    companion object {
        private const val TAG = "ForgotPasswordViewModel"
    }
}