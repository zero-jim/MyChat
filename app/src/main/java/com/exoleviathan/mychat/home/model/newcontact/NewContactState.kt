package com.exoleviathan.mychat.home.model.newcontact

sealed class NewContactState {
    data object Initial : NewContactState()
    data class UserAuthDataStatus(val isAvailable: Boolean) : NewContactState()
    data class AddNewContactStatus(val isAdded: Boolean, val message: String? = null) : NewContactState()
}