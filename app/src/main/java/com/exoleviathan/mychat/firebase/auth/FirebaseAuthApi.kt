package com.exoleviathan.mychat.firebase.auth

import com.exoleviathan.mychat.firebase.model.UserAuthData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

interface FirebaseAuthApi {
    @Throws(Exception::class)
    fun getAuth(): FirebaseAuth

    @Throws(Exception::class)
    suspend fun createNewUserWithEmailAndPassword(email: String, password: String, listener: OnCompleteListener<AuthResult?>?)

    @Throws(Exception::class)
    suspend fun updateUserProfileInformation(profileUpdates: UserProfileChangeRequest, listener: OnCompleteListener<Void>?)

    @Throws(Exception::class)
    suspend fun signInUserWithEmailAndPassword(email: String, password: String, listener: OnCompleteListener<AuthResult?>?)

    @Throws(Exception::class)
    suspend fun isUserEmailVerified(isVerified: (Boolean) -> Unit)

    @Throws(Exception::class)
    suspend fun sendEmailVerification(listener: OnCompleteListener<Void>?)

    @Throws(Exception::class)
    suspend fun getUserAuthInformation(userInformation: (UserAuthData?) -> Unit)

    @Throws(Exception::class)
    suspend fun sendPasswordResetEmail(email: String, listener: OnCompleteListener<Void>?)

    @Throws(Exception::class)
    fun signOutUser(): Boolean
}