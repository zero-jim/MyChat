package com.exoleviathan.mychat.home.model.home

sealed class HomeIntent {
    data object AddChatRoomListener : HomeIntent()
    data object RemoveChatRoomListener : HomeIntent()
}