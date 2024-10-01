package com.exoleviathan.mychat.home.ui.newcontact

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exoleviathan.mychat.auth.domain.validator.EmailFieldValidator
import com.exoleviathan.mychat.auth.models.ValidationMessages
import com.exoleviathan.mychat.firebase.auth.FirebaseAuthHelper
import com.exoleviathan.mychat.firebase.firestore.FirebaseFirestoreHelper
import com.exoleviathan.mychat.firebase.firestore.fields.UserProfileInfoDataFields
import com.exoleviathan.mychat.firebase.model.UserAuthData
import com.exoleviathan.mychat.home.model.newcontact.NewContactData
import com.exoleviathan.mychat.home.model.newcontact.NewContactIntent
import com.exoleviathan.mychat.home.model.newcontact.NewContactState
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class NewContactViewModel : ViewModel() {
    val emailText = MutableLiveData<String?>(null)

    private var userAuthData: UserAuthData? = null

    val newContactIntent = Channel<NewContactIntent>(Channel.UNLIMITED)
    val newContactState = MutableStateFlow<NewContactState>(NewContactState.Initial)
    val newContactData = MutableSharedFlow<NewContactData>(1)

    init {
        Logger.d(TAG, "init", moduleName = ModuleNames.HOME.value)

        viewModelScope.launch {

            newContactIntent.consumeAsFlow().collect { intent ->
                Logger.i(TAG, "init", "intent: $intent", ModuleNames.HOME.value)

                when (intent) {
                    NewContactIntent.FetchUserAuthData -> {
                        viewModelScope.launch(Dispatchers.IO) {
                            getUserAuthData()
                        }
                    }

                    NewContactIntent.AddContactListListener -> {
                        viewModelScope.launch(Dispatchers.IO) {
                            addContactsListUpdateListener()
                        }
                    }

                    NewContactIntent.RemoveContactListListener -> {
                        removeContactListListener()
                    }

                    NewContactIntent.AddNewContact -> {
                        viewModelScope.launch(Dispatchers.IO) {
                            addNewContact()
                        }
                    }
                }
            }
        }
    }

    private suspend fun getUserAuthData() {
        Logger.d(TAG, "getUserAuthData", moduleName = ModuleNames.HOME.value)

        FirebaseAuthHelper.getInstance().getUserAuthInformation { authData ->
            Logger.d(TAG, "getUserAuthData", "authData: $authData", ModuleNames.HOME.value)

            userAuthData = authData
            newContactState.value = NewContactState.UserAuthDataStatus(authData != null)
        }
    }

    private suspend fun addContactsListUpdateListener() {
        Logger.d(TAG, "addContactsListUpdateListener", moduleName = ModuleNames.HOME.value)

        userAuthData?.let {

            FirebaseFirestoreHelper.getInstance().addUserProfileInformationUpdateListener(it.uid, TAG) { documentSnap, exception ->
                Logger.i(TAG, "addContactsListUpdateListener", "documentSnap size: ${documentSnap?.data?.size}", ModuleNames.HOME.value)

                if (exception != null) {
                    Logger.e(TAG, "addContactsListUpdateListener", "error: ${exception.message}", ModuleNames.HOME.value)
                    exception.printStackTrace()
                    return@addUserProfileInformationUpdateListener
                }

                val authData = documentSnap?.toObject(UserAuthData::class.java)
                val friendList = authData?.friendList

                Logger.i(TAG, "addContactsListUpdateListener", "friendList size: ${friendList?.size}", ModuleNames.HOME.value)
                viewModelScope.launch(Dispatchers.IO) {
                    newContactData.tryEmit(NewContactData.Initial)
                    getContactsInformation(friendList)
                }
            }
        }
    }

    private suspend fun getContactsInformation(contacts: List<String>?) {
        Logger.d(TAG, "getContactsInformation", moduleName = ModuleNames.HOME.value)

        contacts?.forEach {

            FirebaseFirestoreHelper.getInstance().getUserProfileInformation(it, UserProfileInfoDataFields.USER_ID.fieldName) { task ->
                Logger.i(TAG, "getContactsInformation", "task: ${task.isSuccessful}")

                task.addOnSuccessListener { querySnap ->
                    if (querySnap == null || querySnap.isEmpty) {
                        Logger.e(TAG, "getContactsInformation", "no user found with this uid", ModuleNames.HOME.value)
                    } else {
                        val userAuthData = querySnap.documents[0].toObject(UserAuthData::class.java)
                        Logger.d(TAG, "getContactsInformation", "user found: $userAuthData", ModuleNames.HOME.value)

                        userAuthData?.let { authData ->
                            val contactInfo = Pair(authData.uid, authData.displayName)
                            Logger.d(TAG, "getContactsInformation", "contactInfo: $contactInfo", ModuleNames.HOME.value)

                            newContactData.tryEmit(NewContactData.Contact(contactInfo))
                        } ?: run {
                            Logger.e(TAG, "getContactsInformation", "friend user id is null", ModuleNames.HOME.value)
                        }
                    }
                }
            }
        }
    }

    private fun removeContactListListener() {
        Logger.d(TAG, "removeContactListListener", moduleName = ModuleNames.HOME.value)

        FirebaseFirestoreHelper.getInstance().removeUserProfileUpdateListener(TAG)
    }

    private suspend fun getUserProfileInformationFromEmail(email: String, listener: OnCompleteListener<QuerySnapshot?>) {
        Logger.d(TAG, "addNewContact", "email: $email", moduleName = ModuleNames.HOME.value)

        FirebaseFirestoreHelper.getInstance().getUserProfileInformation(email, UserProfileInfoDataFields.EMAIL.fieldName, listener)
    }

    private fun checkIfEmailFieldIsValid(): Boolean {
        emailText.postValue("")

        return if (TextUtils.isEmpty(emailText.value)) {
            Logger.i(TAG, "checkIfEmailFieldIsValid", "no user email address is provided", ModuleNames.HOME.value)
            false
        } else {
            Logger.i(TAG, "checkIfEmailFieldIsValid", "user email address is provided", ModuleNames.HOME.value)
            val validationMessage = EmailFieldValidator(ValidationMessages.CORRECT_EMAIL_FORMAT_PROVIDED).validate(emailText.value)

            Logger.i(TAG, "checkIfEmailFieldIsValid", "validationMessage: $validationMessage", ModuleNames.HOME.value)
            validationMessage == ValidationMessages.CORRECT_EMAIL_FORMAT_PROVIDED
        }
    }

    private fun isCurrentUserEmailAddress(currentEmailAddress: String?): Boolean {
        return if (TextUtils.equals(currentEmailAddress, emailText.value)) {
            Logger.i(TAG, "isCurrentUserEmailAddress", "user email address provided", ModuleNames.HOME.value)
            true
        } else {
            Logger.i(TAG, "isCurrentUserEmailAddress", "new email address provided", ModuleNames.HOME.value)
            false
        }
    }

    private suspend fun addNewContact() {
        Logger.d(TAG, "addNewContact", "authData: $userAuthData", moduleName = ModuleNames.HOME.value)

        if (checkIfEmailFieldIsValid().not()) {
            newContactState.emit(NewContactState.AddNewContactStatus(false, "Email text field is empty."))
            return
        }

        if (isCurrentUserEmailAddress(userAuthData?.email)) {
            newContactState.emit(NewContactState.AddNewContactStatus(false, "Current user email address is provided."))
            return
        }

        getUserProfileInformationFromEmail(emailText.value ?: "") { task ->
            Logger.i(TAG, "addNewContact", "task: ${task.isSuccessful}")

            task.addOnSuccessListener { querySnap ->
                if (querySnap == null || querySnap.isEmpty) {
                    Logger.e(TAG, "addNewContact", "no user found with this email address", ModuleNames.HOME.value)

                    newContactState.value = NewContactState.AddNewContactStatus(false, "No user found with this email address.")
                } else {
                    Logger.d(TAG, "addNewContact", "user found for email ${emailText.value}", ModuleNames.HOME.value)
                    val user = querySnap.documents[0].toObject(UserAuthData::class.java)

                    Logger.d(TAG, "addNewContact", "user: $user", ModuleNames.HOME.value)
                    user?.let {
                        viewModelScope.launch(Dispatchers.IO) {
                            FirebaseFirestoreHelper.getInstance().updateUserProfileInformation(userAuthData?.uid ?: "", UserProfileInfoDataFields.FRIEND_LIST.fieldName, it.uid) { task ->
                                if (task.isSuccessful) {
                                    newContactState.value = NewContactState.AddNewContactStatus(false, "Update contact information successful.")
                                } else {
                                    newContactState.value = NewContactState.AddNewContactStatus(false, "Update contact information failed.")
                                }
                            }
                        }
                    } ?: run {
                        Logger.e(TAG, "addNewContact", "friend user id is null", ModuleNames.HOME.value)
                        newContactState.value = NewContactState.AddNewContactStatus(false, "No user found with this email address.")
                    }
                }
            }

            task.addOnFailureListener {
                newContactState.value = NewContactState.AddNewContactStatus(false, "No user found with this email address.")
            }
        }
    }

    fun deInit() {
        Logger.d(TAG, "deInit", moduleName = ModuleNames.HOME.value)

        newContactIntent.close()
    }

    companion object {
        private const val TAG = "NewContactViewModel"
    }
}