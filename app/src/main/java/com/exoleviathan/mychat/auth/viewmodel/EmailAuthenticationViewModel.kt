package com.exoleviathan.mychat.auth.viewmodel

import androidx.lifecycle.ViewModel
import com.exoleviathan.mychat.firebase.auth.FirebaseAuthenticationHelper
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames

class EmailAuthenticationViewModel : ViewModel() {

    fun isUserEmailVerified(): Boolean {
        Logger.i(TAG, "isUserEmailVerified", moduleName = ModuleNames.AUTH.value)
        return FirebaseAuthenticationHelper.getInstance()?.isUserEmailVerified() == true
    }

    fun reloadUser(reloadAction: (Boolean) -> Unit) {
        Logger.i(TAG, "reloadUser", moduleName = ModuleNames.AUTH.value)
        FirebaseAuthenticationHelper.getInstance()?.getFirebaseAuth()?.currentUser?.reload()?.addOnCompleteListener {
            if (it.isSuccessful) {
                reloadAction.invoke(true)
            } else {
                reloadAction.invoke(false)
            }
        }
    }

    companion object {
        private const val TAG = "EmailAuthenticationViewModel"
    }
}