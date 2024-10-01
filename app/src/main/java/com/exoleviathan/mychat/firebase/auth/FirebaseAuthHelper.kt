package com.exoleviathan.mychat.firebase.auth

import com.exoleviathan.mychat.firebase.model.UserAuthData
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth

class FirebaseAuthHelper private constructor() : FirebaseAuthApi {
    private var auth: FirebaseAuth? = null

    init {
        Logger.d(TAG, "init", moduleName = ModuleNames.FIREBASE_API.value)

        auth = Firebase.auth
    }

    @Throws(Exception::class)
    override fun getAuth(): FirebaseAuth {
        Logger.d(TAG, "getAuth", moduleName = ModuleNames.FIREBASE_API.value)

        return auth ?: Firebase.auth.also {
            Logger.i(TAG, "getAuth", "auth is null, assigning auth", ModuleNames.FIREBASE_API.value)
            auth = it
        }
    }

    @Throws(Exception::class)
    override suspend fun createNewUserWithEmailAndPassword(email: String, password: String, listener: OnCompleteListener<AuthResult?>?) {
        Logger.d(TAG, "createNewUserWithEmailAndPassword", "email: $email password: $password", ModuleNames.FIREBASE_API.value)

        listener?.let {
            auth?.createUserWithEmailAndPassword(email, password)?.addOnCompleteListener(it)
        } ?: run {
            auth?.createUserWithEmailAndPassword(email, password)
        }
    }

    override suspend fun updateUserProfileInformation(profileUpdates: UserProfileChangeRequest, listener: OnCompleteListener<Void>?) {
        Logger.d(TAG, "updateUserProfileInformation", "profileUpdates: $profileUpdates", ModuleNames.FIREBASE_API.value)

        listener?.let {
            auth?.currentUser?.updateProfile(profileUpdates)?.addOnCompleteListener(listener)
        } ?: run {
            auth?.currentUser?.updateProfile(profileUpdates)
        }
    }

    @Throws(Exception::class)
    override suspend fun signInUserWithEmailAndPassword(email: String, password: String, listener: OnCompleteListener<AuthResult?>?) {
        Logger.d(TAG, "signInUserWithEmailAndPassword", "email: $email password: $password", ModuleNames.FIREBASE_API.value)

        listener?.let {
            auth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener(it)
        } ?: run {
            auth?.signInWithEmailAndPassword(email, password)
        }
    }

    @Throws(Exception::class)
    override suspend fun isUserEmailVerified(isVerified: (Boolean) -> Unit) {
        Logger.d(TAG, "isUserEmailVerified", moduleName = ModuleNames.FIREBASE_API.value)

        auth?.currentUser?.let { user ->
            Logger.i(TAG, "isUserEmailVerified", "user is already signed in, reloading", ModuleNames.FIREBASE_API.value)

            user.reload().addOnCompleteListener {
                Logger.i(TAG, "isUserEmailVerified", "user reloading task completed", ModuleNames.FIREBASE_API.value)
                isVerified.invoke(user.isEmailVerified)
            }.addOnCanceledListener {
                Logger.i(TAG, "isUserEmailVerified", "user reloading task cancelled", ModuleNames.FIREBASE_API.value)
                isVerified.invoke(false)
            }
        } ?: run {
            Logger.e(TAG, "isUserEmailVerified", "user is not signed in", ModuleNames.FIREBASE_API.value)
            isVerified.invoke(false)
        }
    }

    @Throws(Exception::class)
    override suspend fun sendEmailVerification(listener: OnCompleteListener<Void>?) {
        Logger.d(TAG, "sendEmailVerification", moduleName = ModuleNames.FIREBASE_API.value)

        listener?.let {
            auth?.currentUser?.sendEmailVerification()?.addOnCompleteListener(it)
        } ?: run {
            auth?.currentUser?.sendEmailVerification()
        }
    }

    @Throws(Exception::class)
    private fun isUserSignedIn(isSignedIn: (Boolean) -> Unit) {
        Logger.d(TAG, "isUserSignedIn", moduleName = ModuleNames.FIREBASE_API.value)

        auth?.currentUser?.let { user ->
            Logger.i(TAG, "isUserSignedIn", "user is already signed in, reloading", ModuleNames.FIREBASE_API.value)

            user.reload().addOnCompleteListener {
                Logger.i(TAG, "isUserSignedIn", "user reloading task completed", ModuleNames.FIREBASE_API.value)
                isSignedIn.invoke(true)
            }.addOnCanceledListener {
                Logger.i(TAG, "isUserSignedIn", "user reloading task cancelled", ModuleNames.FIREBASE_API.value)
                isSignedIn.invoke(false)
            }
        } ?: run {
            Logger.i(TAG, "isUserSignedIn", "user is not signed in", ModuleNames.FIREBASE_API.value)
            isSignedIn.invoke(false)
        }
    }

    @Throws(Exception::class)
    override suspend fun getUserAuthInformation(userInformation: (UserAuthData?) -> Unit) {
        Logger.d(TAG, "getUserAuthInformation", moduleName = ModuleNames.FIREBASE_API.value)

        auth?.currentUser?.let { user ->
            isUserSignedIn { isSignedIn ->
                if (isSignedIn) {
                    val uid = user.uid
                    val email = user.email
                    val displayName = user.displayName
                    val isEmailVerified = user.isEmailVerified
                    val photoUrl = user.photoUrl?.toString()

                    val data = UserAuthData(
                        uid = uid,
                        email = email,
                        isEmailVerified = isEmailVerified,
                        displayName = displayName,
                        photoUrl = photoUrl
                    )
                    Logger.d(TAG, "getUserAuthInformation", "userData = $data", ModuleNames.FIREBASE_API.value)
                    userInformation.invoke(data)
                } else {
                    Logger.e(TAG, "getUserAuthInformation", "user is not signed in", ModuleNames.FIREBASE_API.value)
                    userInformation.invoke(null)
                }
            }
        } ?: run {
            Logger.w(TAG, "getUserAuthInformation", "user is not signed in", ModuleNames.FIREBASE_API.value)
            userInformation.invoke(null)
        }
    }

    @Throws(Exception::class)
    override suspend fun sendPasswordResetEmail(email: String, listener: OnCompleteListener<Void>?) {
        Logger.d(TAG, "sendPasswordResetEmail", "email: $email", ModuleNames.FIREBASE_API.value)

        listener?.let {
            auth?.sendPasswordResetEmail(email)?.addOnCompleteListener(listener)
        } ?: run {
            auth?.sendPasswordResetEmail(email)
        }
    }

    @Throws(Exception::class)
    override fun signOutUser(): Boolean {
        Logger.d(TAG, "signOutUser", moduleName = ModuleNames.FIREBASE_API.value)

        auth?.signOut()
        return true
    }

    companion object {
        private const val TAG = "FirebaseAuthHelper"

        private var instance: FirebaseAuthHelper? = null
        private val lock = Any()

        @Synchronized
        fun getInstance(): FirebaseAuthApi {
            synchronized(lock) {
                return instance ?: synchronized(lock) {
                    Logger.i(TAG, "getInstance", "instance is null, creating new instance", ModuleNames.FIREBASE_API.value)

                    FirebaseAuthHelper().also {
                        Logger.i(TAG, "getInstance", "setting up the created instance", ModuleNames.FIREBASE_API.value)
                        instance = it
                    }
                }
            }
        }
    }
}