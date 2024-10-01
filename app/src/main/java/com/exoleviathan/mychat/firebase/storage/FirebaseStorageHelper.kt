package com.exoleviathan.mychat.firebase.storage

import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class FirebaseStorageHelper private constructor() : FirebaseStorageApi {
    private var storage: FirebaseStorage? = null

    init {
        Logger.d(TAG, "init", moduleName = ModuleNames.FIREBASE_API.value)

        storage = Firebase.storage
    }

    override fun getStorage(): FirebaseStorage {
        Logger.d(TAG, "getStorage", moduleName = ModuleNames.FIREBASE_API.value)

        return storage ?: run {
            Firebase.storage.also {
                storage = it
            }
        }
    }

    companion object {
        private const val TAG = "FirebaseStorageHelper"

        private var instance: FirebaseStorageHelper? = null
        private val lock = Any()

        @Synchronized
        fun getInstance(): FirebaseStorageApi {
            synchronized(lock) {
                return instance ?: synchronized(lock) {
                    Logger.i(TAG, "getInstance", "instance is null, creating new instance", ModuleNames.FIREBASE_API.value)

                    FirebaseStorageHelper().also {
                        Logger.i(TAG, "getInstance", "setting up the instance", ModuleNames.FIREBASE_API.value)
                        instance = it
                    }
                }
            }
        }
    }
}