package com.exoleviathan.mychat.firebase.firestore

import com.exoleviathan.mychat.firebase.model.ChatRoomData
import com.exoleviathan.mychat.firebase.model.MessageData
import com.exoleviathan.mychat.firebase.model.UserAuthData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

interface FirebaseFirestoreApi {
    @Throws(Exception::class)
    fun getFirestore(): FirebaseFirestore

    @Throws(Exception::class)
    suspend fun createUserProfileInformation(userAuthData: UserAuthData, listener: OnCompleteListener<Void>?)

    @Throws(Exception::class)
    suspend fun getUserProfileInformation(query: String, queryField: String, listener: OnCompleteListener<QuerySnapshot?>?)

    @Throws(Exception::class)
    suspend fun <T> updateUserProfileInformation(userId: String, fieldName: String, fieldValue: T, listener: OnCompleteListener<Void>)

    @Throws(Exception::class)
    suspend fun addUserProfileInformationUpdateListener(userId: String, listenerName: String, listener: EventListener<DocumentSnapshot?>)

    @Throws(Exception::class)
    fun removeUserProfileUpdateListener(listenerName: String)

    @Throws(Exception::class)
    suspend fun saveFCMToken(userId: String, token: String, listener: OnCompleteListener<Void>?)

    @Throws(Exception::class)
    suspend fun getFCMToken(userId: String, listener: OnCompleteListener<DocumentSnapshot?>?)

    @Throws(Exception::class)
    suspend fun setChatRoomData(chatRoomData: ChatRoomData, listener: OnCompleteListener<Void>)

    @Throws(Exception::class)
    suspend fun updateChatRoomReadStatus(chatRoomId: String, userId: String, status: Boolean, listener: OnCompleteListener<Void>?)

    @Throws(Exception::class)
    suspend fun addChatRoomUpdateListener(userId: String, listenerName: String, listener: EventListener<QuerySnapshot?>)

    @Throws(Exception::class)
    fun removeChatRoomUpdateListener(listenerName: String)

    @Throws(Exception::class)
    suspend fun sendMessage(msgData: MessageData, listener: OnCompleteListener<DocumentReference?>)

    @Throws(Exception::class)
    suspend fun addMessageUpdateListener(chatRoomId: String, listenerName: String, listener: EventListener<QuerySnapshot?>)

    @Throws(Exception::class)
    fun removeMessageUpdateListener(listenerName: String)
}