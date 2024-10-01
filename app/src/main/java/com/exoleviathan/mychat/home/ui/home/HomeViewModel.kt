package com.exoleviathan.mychat.home.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exoleviathan.mychat.firebase.auth.FirebaseAuthHelper
import com.exoleviathan.mychat.firebase.firestore.FirebaseFirestoreHelper
import com.exoleviathan.mychat.firebase.firestore.fields.UserProfileInfoDataFields
import com.exoleviathan.mychat.firebase.model.ChatRoomData
import com.exoleviathan.mychat.firebase.model.UserAuthData
import com.exoleviathan.mychat.home.model.home.HomeIntent
import com.exoleviathan.mychat.home.model.home.HomeState
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private var userAuthData: UserAuthData? = null

    val homeIntent = Channel<HomeIntent>(Channel.UNLIMITED)
    val homeState = MutableStateFlow<HomeState>(HomeState.Initial)

    init {
        Logger.d(TAG, "init", moduleName = ModuleNames.HOME.value)

        viewModelScope.launch {

            homeIntent.consumeAsFlow().collect { intent ->
                Logger.i(TAG, "init", "intent: $intent", ModuleNames.HOME.value)

                when (intent) {
                    HomeIntent.AddChatRoomListener -> {
                        Logger.d(TAG, "init", "AddChatRoomListener", ModuleNames.HOME.value)

                        viewModelScope.launch(Dispatchers.IO) {

                            userAuthData?.let {
                                Logger.i(TAG, "init", "AddChatRoomListener::userAuthData is available", ModuleNames.HOME.value)
                                addChatRoomListener()
                            } ?: run {
                                Logger.i(TAG, "init", "AddChatRoomListener::userAuthData is unavailable", ModuleNames.HOME.value)

                                getUserAuthInformation {
                                    Logger.i(TAG, "init", "AddChatRoomListener::user information load success: $it", ModuleNames.HOME.value)

                                    if (it.not()) {
                                        homeState.value = HomeState.FailedToLoadUserInformation("Failed to load user information.")
                                    } else {
                                        viewModelScope.launch(Dispatchers.IO) {
                                            addChatRoomListener()
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HomeIntent.RemoveChatRoomListener -> {
                        removeChatRoomListener()
                    }
                }
            }
        }
    }

    private suspend fun getUserAuthInformation(isSuccess: (Boolean) -> Unit) {
        Logger.d(TAG, "getUserAuthInformation", moduleName = ModuleNames.HOME.value)

        FirebaseAuthHelper.getInstance().getUserAuthInformation { authData ->
            Logger.d(TAG, "getUserAuthInformation", "authData: $authData", ModuleNames.HOME.value)

            userAuthData = authData
            isSuccess.invoke(authData != null)
        }
    }

    private suspend fun addChatRoomListener() {
        Logger.d(TAG, "addChatRoomListener", moduleName = ModuleNames.HOME.value)

        userAuthData?.let { authData ->
            FirebaseFirestoreHelper.getInstance().addChatRoomUpdateListener(authData.uid, TAG) { querySnaps, exception ->
                Logger.i(TAG, "addChatRoomListener", "querySnaps size: ${querySnaps?.documents?.size}", ModuleNames.HOME.value)

                if (querySnaps == null || querySnaps.documentChanges.size == 0) {
                    homeState.value = HomeState.OnChatRoomListUpdated(arrayListOf())
                    Logger.w(TAG, "addChatRoomListener", "query snapshot is null or empty", ModuleNames.HOME.value)
                    return@addChatRoomUpdateListener
                }

                if (exception != null) {
                    homeState.value = HomeState.OnChatRoomListUpdated(arrayListOf())
                    Logger.e(TAG, "addChatRoomListener", "error: ${exception.message}", ModuleNames.HOME.value)
                    exception.printStackTrace()
                    return@addChatRoomUpdateListener
                }

                querySnaps.documentChanges.forEach {
                    Logger.i(TAG, "addChatRoomListener", "querySnapshots: ${it.type}", ModuleNames.HOME.value)

                    val chatRooms = arrayListOf<ChatRoomData?>()
                    querySnaps.documents.forEach { docSnap ->
                        val chatRoomData = docSnap.toObject(ChatRoomData::class.java)
                        chatRooms.add(chatRoomData)
                    }

                    Logger.i(TAG, "addChatRoomListener", "chatRoomsList size: ${chatRooms.size}", ModuleNames.HOME.value)
                    homeState.value = HomeState.OnChatRoomListUpdated(chatRooms)
                }
            }
        }
    }

    private fun removeChatRoomListener() {
        Logger.d(TAG, "removeChatRoomListener", moduleName = ModuleNames.HOME.value)

        userAuthData?.let {
            FirebaseFirestoreHelper.getInstance().removeChatRoomUpdateListener(TAG)
        }
    }

    fun getChatReadStatus(data: ChatRoomData?): Boolean {
        Logger.d(TAG, "getChatReadStatus", moduleName = ModuleNames.HOME.value)

        return userAuthData?.let {
            data?.readStatus?.get(it.uid) ?: false
        } ?: false
    }

    fun getChatRoomInformation(participantList: ArrayList<String?>?, authData: (UserAuthData?) -> Unit) {
        Logger.d(TAG, "getChatRoomInformation", "participantList: $participantList", ModuleNames.HOME.value)

        userAuthData?.let {
            participantList?.remove(it.uid)

            viewModelScope.launch(Dispatchers.IO) {

                FirebaseFirestoreHelper.getInstance().getUserProfileInformation(participantList?.get(0) ?: "", UserProfileInfoDataFields.USER_ID.fieldName) { task ->
                    Logger.i(TAG, "getChatRoomInformation", "response: ${task.isSuccessful}")

                    task.addOnSuccessListener { querySnap ->
                        val recipientAuthData = querySnap?.documents?.get(0)?.toObject(UserAuthData::class.java)
                        Logger.d(TAG, "getChatRoomInformation", "recipientAuthData: $recipientAuthData", ModuleNames.HOME.value)
                        authData.invoke(recipientAuthData)
                    }

                    task.addOnFailureListener { ex ->
                        Logger.e(TAG, "getChatRoomInformation", "recipientAuthData retrieve failed, error: ${ex.message}", ModuleNames.HOME.value)
                        ex.printStackTrace()
                        authData.invoke(null)
                    }
                }
            }
        }
    }

    fun deInit() {
        Logger.d(TAG, "deInit", moduleName = ModuleNames.HOME.value)

        homeIntent.close()
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}