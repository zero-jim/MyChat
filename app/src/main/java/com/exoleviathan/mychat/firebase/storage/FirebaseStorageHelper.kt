package com.exoleviathan.mychat.firebase.storage

import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class FirebaseStorageHelper {
    private var storage: FirebaseStorage? = null

    fun initialize() {
        storage = Firebase.storage(Firebase.app)
    }
}