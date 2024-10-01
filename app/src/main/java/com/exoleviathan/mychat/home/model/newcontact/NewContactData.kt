package com.exoleviathan.mychat.home.model.newcontact

sealed class NewContactData {
    data object Initial : NewContactData()
    data class Contact(val info: Pair<String?, String?>) : NewContactData()
}