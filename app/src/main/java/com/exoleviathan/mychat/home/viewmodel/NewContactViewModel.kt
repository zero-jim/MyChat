package com.exoleviathan.mychat.home.viewmodel

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.exoleviathan.mychat.firebase.auth.FirebaseAuthenticationHelper
import com.exoleviathan.mychat.firebase.firestore.FirebaseFirestoreHelper
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames

class NewContactViewModel : ViewModel() {
    val emailText = MutableLiveData<String?>(null)
    private val _updatedFriendList = MutableLiveData<ArrayList<Pair<String, String>>>(arrayListOf())

    val updatedFriendList: LiveData<ArrayList<Pair<String, String>>> = _updatedFriendList

    init {
        val user = FirebaseAuthenticationHelper.getInstance()?.getUserInformation()

        user?.let {
            FirebaseFirestoreHelper.getInstance()?.addFriendListUpdateListener(it.uid, TAG) { list ->
                Logger.i(TAG, "init", "friendList: $list", ModuleNames.HOME.value)
                _updatedFriendList.postValue(list)
            }
        }
    }

    fun addNewUser(result: (Boolean, String) -> Unit) {
        Logger.d(TAG, "addNewUser", moduleName = ModuleNames.HOME.value)

        if (TextUtils.isEmpty(emailText.value)) {
            emailText.postValue("")
            Logger.i(TAG, "addNewUser", "user email address is not provided", ModuleNames.HOME.value)
            result.invoke(false, "Email address is not provided.")
        } else {
            emailText.postValue("")
            FirebaseFirestoreHelper.getInstance()
                ?.addAsFriend(emailText.value ?: "") { res, msg ->
                    result.invoke(res, msg)
                }
        }
    }

    fun deInit() {
        FirebaseFirestoreHelper.getInstance()?.removeFriendListUpdateListener(TAG)
    }

    companion object {
        private const val TAG = "NewContactViewModel"
    }
}