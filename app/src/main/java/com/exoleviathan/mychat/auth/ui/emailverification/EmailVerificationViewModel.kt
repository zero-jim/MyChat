package com.exoleviathan.mychat.auth.ui.emailverification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exoleviathan.mychat.auth.models.emailverification.EmailVerificationIntent
import com.exoleviathan.mychat.auth.models.emailverification.EmailVerificationState
import com.exoleviathan.mychat.firebase.auth.FirebaseAuthHelper
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class EmailVerificationViewModel : ViewModel() {
    val emailVerificationIntent = Channel<EmailVerificationIntent>(Channel.UNLIMITED)
    val emailVerificationStates = MutableStateFlow<EmailVerificationState>(EmailVerificationState.Initial)

    init {
        viewModelScope.launch {

            emailVerificationIntent.consumeAsFlow().collect { intent ->
                Logger.i(TAG, "init", "intent: $intent", ModuleNames.AUTH.value)

                when (intent) {
                    EmailVerificationIntent.CheckIfEmailVerified -> {
                        viewModelScope.launch(Dispatchers.IO) {
                            isUserEmailVerified()
                        }
                    }

                    EmailVerificationIntent.SendUserEmailVerification -> {
                        viewModelScope.launch(Dispatchers.IO) {
                            sendUserEmailVerification()
                        }
                    }
                }
            }
        }
    }

    private suspend fun sendUserEmailVerification() {
        Logger.d(TAG, "sendUserEmailVerification", moduleName = ModuleNames.AUTH.value)

        FirebaseAuthHelper.getInstance().sendEmailVerification { task ->
            emailVerificationStates.value = EmailVerificationState.SendEmailVerificationStatus(task.isSuccessful)
        }
    }

    private suspend fun isUserEmailVerified() {
        Logger.d(TAG, "isUserEmailVerified", moduleName = ModuleNames.AUTH.value)

        FirebaseAuthHelper.getInstance().isUserEmailVerified { isVerified ->
            Logger.i(TAG, "isUserEmailVerified", "isVerified: $isVerified", ModuleNames.AUTH.value)
            emailVerificationStates.value = EmailVerificationState.UserEmailVerificationStatus(isVerified)
        }
    }

    companion object {
        private const val TAG = "EmailVerificationViewModel"
    }
}