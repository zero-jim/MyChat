package com.exoleviathan.mychat.firebase.storage

import com.google.firebase.storage.FirebaseStorage

interface FirebaseStorageApi {
    fun getStorage(): FirebaseStorage
}