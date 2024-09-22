package com.exoleviathan.mychat.firebase.auth

import com.exoleviathan.mychat.firebase.model.UserData
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class FirebaseAuthenticationHelper private constructor() {
    private var auth: FirebaseAuth? = null

    private fun initialize() {
        Logger.d(TAG, "initialize", moduleName = ModuleNames.FIREBASE_API.value)
        auth = Firebase.auth
    }

    fun getFirebaseAuth(): FirebaseAuth? {
        Logger.d(TAG, "getFirebaseAuth", moduleName = ModuleNames.FIREBASE_API.value)

        return auth
    }

    private fun isUserAlreadySignedIn(): Boolean {
        Logger.d(TAG, "isUserAlreadySignedIn", moduleName = ModuleNames.FIREBASE_API.value)

        return if (auth?.currentUser != null) {
            Logger.i(TAG, "isUserAlreadySignedIn", "user already signed in reloading", ModuleNames.FIREBASE_API.value)
            auth?.currentUser?.reload()
            true
        } else {
            Logger.e(TAG, "isUserAlreadySignedIn", "user not signed in", ModuleNames.FIREBASE_API.value)
            false
        }
    }

    fun isUserEmailVerified(): Boolean {
        Logger.d(TAG, "isUserEmailVerified", moduleName = ModuleNames.FIREBASE_API.value)

        return if (auth?.currentUser != null) {
            Logger.i(TAG, "isUserEmailVerified", "user already signed in reloading", ModuleNames.FIREBASE_API.value)
            auth?.currentUser?.reload()
            auth?.currentUser?.isEmailVerified ?: false
        } else {
            Logger.e(TAG, "isUserEmailVerified", "user not signed in", ModuleNames.FIREBASE_API.value)
            false
        }
    }

    fun getUserInformation(): UserData? {
        return if (isUserAlreadySignedIn()) {
            Logger.i(TAG, "getUserInformation", "user is signed in", ModuleNames.FIREBASE_API.value)

            val email = auth?.currentUser?.email
            val displayName = auth?.currentUser?.displayName
            val uid = auth?.currentUser?.uid
            val photoUrl = auth?.currentUser?.photoUrl?.toString()
            val isEmailVerified = auth?.currentUser?.isEmailVerified
            val data = UserData(uid = uid ?: "unknown_uid", displayName = displayName, photoUrl = photoUrl, email = email, isEmailVerified = isEmailVerified)

            Logger.d(TAG, "getUserInformation", "userData = $data", ModuleNames.FIREBASE_API.value)
            data
        } else {
            Logger.e(TAG, "getUserInformation", "user is not signed in", ModuleNames.FIREBASE_API.value)
            null
        }
    }

    fun createUserWithEmailAndPassword(email: String, password: String, listener: OnCompleteListener<AuthResult?>) {
        Logger.d(TAG, "signUpNewUserWithEmailAndPassword", "email: $email password: $password", ModuleNames.FIREBASE_API.value)

        auth?.createUserWithEmailAndPassword(email, password)?.addOnCompleteListener(listener)
    }

    fun signInWithEmailAndPassword(email: String, password: String, listener: OnCompleteListener<AuthResult?>) {
        Logger.d(TAG, "signInWithEmailAndPassword", "email: $email password: $password", ModuleNames.FIREBASE_API.value)

        auth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener(listener)
    }

    fun sendEmailVerification() {
        Logger.i(TAG, "sendEmailVerification", "email verification code is sent to email address", ModuleNames.FIREBASE_API.value)
        auth?.currentUser?.sendEmailVerification()
    }

    fun sendPasswordResetEmail(email: String, listener: OnCompleteListener<Void>) {
        auth?.sendPasswordResetEmail(email)?.addOnCompleteListener(listener)
    }

    fun signOutUser() {
        Logger.d(TAG, "signOutUser", moduleName = ModuleNames.FIREBASE_API.value)
        auth?.signOut()
    }

    companion object {
        private const val TAG = "FirebaseAuthenticationHelper"
        private var helper: FirebaseAuthenticationHelper? = null
        private val lock = Any()

        @Synchronized
        fun getInstance(): FirebaseAuthenticationHelper? {
            synchronized(lock) {
                return helper ?: synchronized(lock) {
                    helper = FirebaseAuthenticationHelper()
                    helper?.initialize()
                    return helper
                }
            }
        }
    }
}