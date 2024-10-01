package com.exoleviathan.mychat.home.model.home

import com.exoleviathan.mychat.firebase.model.ChatRoomData

sealed class HomeState {
    data object Initial : HomeState()
    data class FailedToLoadUserInformation(val message: String?) : HomeState()
    data class OnChatRoomListUpdated(val chatRoomList: List<ChatRoomData?>) : HomeState()
}