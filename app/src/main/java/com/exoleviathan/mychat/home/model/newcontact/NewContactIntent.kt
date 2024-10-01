package com.exoleviathan.mychat.home.model.newcontact

import com.exoleviathan.mychat.firebase.model.UserAuthData

sealed class NewContactIntent {
    data object FetchUserAuthData : NewContactIntent()
    data object AddContactListListener : NewContactIntent()
    data object RemoveContactListListener : NewContactIntent()
    data object AddNewContact : NewContactIntent()
}