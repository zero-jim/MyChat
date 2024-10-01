package com.exoleviathan.mychat.firebase.firestore

import com.exoleviathan.mychat.firebase.firestore.fields.ChatRoomDataFields
import com.exoleviathan.mychat.firebase.firestore.fields.FCMTokenDataFields
import com.exoleviathan.mychat.firebase.firestore.fields.FirestoreCollections
import com.exoleviathan.mychat.firebase.firestore.fields.MessageDataFields
import com.exoleviathan.mychat.firebase.firestore.fields.UserProfileInfoDataFields
import com.exoleviathan.mychat.firebase.model.ChatRoomData
import com.exoleviathan.mychat.firebase.model.MessageData
import com.exoleviathan.mychat.firebase.model.UserAuthData
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query.Direction
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.concurrent.ConcurrentHashMap

class FirebaseFirestoreHelper private constructor() : FirebaseFirestoreApi {
    private var firestore: FirebaseFirestore? = null
    private val friendListListenerMap = ConcurrentHashMap<String, ListenerRegistration?>()
    private val chatRoomListenerMap = ConcurrentHashMap<String, ListenerRegistration?>()
    private val messageListListenerMap = ConcurrentHashMap<String, ListenerRegistration?>()

    init {
        Logger.d(TAG, "init", moduleName = ModuleNames.FIREBASE_API.value)

        firestore = Firebase.firestore
    }

    @Throws(Exception::class)
    override fun getFirestore(): FirebaseFirestore {
        Logger.d(TAG, "getFirestore", moduleName = ModuleNames.FIREBASE_API.value)

        return firestore ?: Firebase.firestore.also {
            firestore = it
        }
    }

    @Throws(Exception::class)
    override suspend fun createUserProfileInformation(userAuthData: UserAuthData, listener: OnCompleteListener<Void>?) {
        Logger.d(TAG, "createUserProfileInformation", "data: $userAuthData", ModuleNames.FIREBASE_API.value)

        firestore?.collection(FirestoreCollections.USER_PROFILE.collectionName)
            ?.document(userAuthData.uid)
            ?.set(userAuthData)
            ?.apply {
                listener?.let {
                    addOnCompleteListener(listener)
                }
            }
    }

    @Throws(Exception::class)
    override suspend fun getUserProfileInformation(query: String, queryField: String, listener: OnCompleteListener<QuerySnapshot?>?) {
        Logger.d(TAG, "getUserProfileInformation", "query: $query queryField: $queryField", ModuleNames.FIREBASE_API.value)

        val collection = firestore?.collection(FirestoreCollections.USER_PROFILE.collectionName)
        collection?.whereEqualTo(queryField, query)
            ?.get()
            ?.apply {
                listener?.let {
                    addOnCompleteListener(it)
                }
            }
    }

    @Throws(Exception::class)
    override suspend fun <T> updateUserProfileInformation(userId: String, fieldName: String, fieldValue: T, listener: OnCompleteListener<Void>) {
        Logger.d(TAG, "updateUserProfileInformation", "userId: $userId fieldName: $fieldName fieldValue: $fieldValue", ModuleNames.FIREBASE_API.value)

        firestore?.collection(FirestoreCollections.USER_PROFILE.collectionName)
            ?.document(userId)
            ?.apply {
                when (fieldName) {
                    UserProfileInfoDataFields.FRIEND_LIST.fieldName -> {
                        update(fieldName, FieldValue.arrayUnion(fieldValue))
                            .addOnCompleteListener(listener)
                    }

                    else -> {
                        update(fieldName, fieldValue as? String?)
                            .addOnCompleteListener(listener)
                    }
                }
            }
    }

    @Throws(Exception::class)
    override suspend fun addUserProfileInformationUpdateListener(userId: String, listenerName: String, listener: EventListener<DocumentSnapshot?>) {
        Logger.d(TAG, "addUserProfileInformationUpdateListener", "userId: $userId listenerName: $listenerName", moduleName = ModuleNames.FIREBASE_API.value)

        val listenerRegistration = firestore?.collection(FirestoreCollections.USER_PROFILE.collectionName)
            ?.document(userId)
            ?.addSnapshotListener(listener)

        friendListListenerMap[listenerName] = listenerRegistration
    }

    @Throws(Exception::class)
    override fun removeUserProfileUpdateListener(listenerName: String) {
        Logger.d(TAG, "removeUserProfileUpdateListener", "listenerName: $listenerName", ModuleNames.FIREBASE_API.value)

        friendListListenerMap[listenerName]?.remove()
        friendListListenerMap.remove(listenerName)
    }

    @Throws(Exception::class)
    override suspend fun saveFCMToken(userId: String, token: String, listener: OnCompleteListener<Void>?) {
        Logger.d(TAG, "saveFCMToken", "userId: $userId token: $token", ModuleNames.FIREBASE_API.value)

        val fcmTokenMap = hashMapOf(
            Pair(FCMTokenDataFields.USER_ID.fieldName, userId),
            Pair(FCMTokenDataFields.FCM_TOKEN_ID.fieldName, token)
        )

        firestore?.collection(FirestoreCollections.FCM_TOKEN.collectionName)
            ?.document(userId)
            ?.set(fcmTokenMap)
            ?.apply {
                listener?.let {
                    addOnCompleteListener(it)
                }
            }
    }

    @Throws(Exception::class)
    override suspend fun getFCMToken(userId: String, listener: OnCompleteListener<DocumentSnapshot?>?) {
        Logger.d(TAG, "getFCMToken", "userId: $userId", ModuleNames.FIREBASE_API.value)

        val document = firestore?.collection(FirestoreCollections.FCM_TOKEN.collectionName)
            ?.document(userId)
        listener?.let {
            document?.get()?.addOnCompleteListener(it)
        } ?: run {
            document?.get()
        }
    }

    @Throws(Exception::class)
    override suspend fun setChatRoomData(chatRoomData: ChatRoomData, listener: OnCompleteListener<Void>) {
        Logger.d(TAG, "setChatRoomData", "chatRoomData: $chatRoomData", ModuleNames.FIREBASE_API.value)

        firestore?.collection(FirestoreCollections.CHAT_ROOMS.collectionName)
            ?.document(chatRoomData.chatRoomId)
            ?.set(chatRoomData)
            ?.addOnCompleteListener(listener)
    }

    @Throws(Exception::class)
    @Suppress("UNCHECKED_CAST")
    override suspend fun updateChatRoomReadStatus(chatRoomId: String, userId: String, status: Boolean, listener: OnCompleteListener<Void>?) {
        Logger.d(TAG, "updateReadStatus", "chatRoomId: $chatRoomId userId: $userId", ModuleNames.FIREBASE_API.value)

        val collection = firestore?.collection(FirestoreCollections.CHAT_ROOMS.collectionName)
        collection?.whereEqualTo(ChatRoomDataFields.CHAT_ROOM_ID.fieldName, chatRoomId)
            ?.get()
            ?.addOnSuccessListener { querySnaps ->
                Logger.i(TAG, "updateReadStatus", "querySnap size: ${querySnaps.documents.size}", ModuleNames.FIREBASE_API.value)

                if (querySnaps.size() > 0) {
                    val readStatus = querySnaps.documents[0].data?.get(ChatRoomDataFields.READ_STATUS.fieldName) as? HashMap<String?, Boolean>
                    readStatus?.set(userId, status)
                    Logger.d(TAG, "updateReadStatus", "readStatus: $readStatus", ModuleNames.FIREBASE_API.value)

                    collection.document(chatRoomId)
                        .update(ChatRoomDataFields.READ_STATUS.fieldName, readStatus)
                        .apply {
                            listener?.let {
                                addOnCompleteListener(listener)
                            }
                        }
                }
            }
    }

    @Throws(Exception::class)
    override suspend fun addChatRoomUpdateListener(userId: String, listenerName: String, listener: EventListener<QuerySnapshot?>) {
        Logger.d(TAG, "addChatRoomUpdateListener", "userId: $userId listenerName: $listenerName", ModuleNames.FIREBASE_API.value)

        val listenerRegistration = firestore?.collection(FirestoreCollections.CHAT_ROOMS.collectionName)
            ?.whereArrayContains(ChatRoomDataFields.PARTICIPANT_ID_LIST.fieldName, userId)
            ?.addSnapshotListener(listener)

        chatRoomListenerMap[listenerName] = listenerRegistration
    }

    @Throws(Exception::class)
    override fun removeChatRoomUpdateListener(listenerName: String) {
        Logger.d(TAG, "removeChatRoomUpdateListener", "listenerName: $listenerName", ModuleNames.FIREBASE_API.value)

        chatRoomListenerMap[listenerName]?.remove()
        chatRoomListenerMap.remove(listenerName)
    }

    @Throws(Exception::class)
    override suspend fun sendMessage(msgData: MessageData, listener: OnCompleteListener<DocumentReference?>) {
        Logger.d(TAG, "sendMessage", "msgData: $msgData", ModuleNames.FIREBASE_API.value)

        firestore?.collection(FirestoreCollections.CHAT_ROOMS.collectionName)
            ?.document(msgData.chatRoomId)
            ?.collection(FirestoreCollections.MESSAGES.collectionName)
            ?.add(msgData)
            ?.addOnCompleteListener(listener)
    }

    @Throws(Exception::class)
    override suspend fun addMessageUpdateListener(chatRoomId: String, listenerName: String, listener: EventListener<QuerySnapshot?>) {
        Logger.d(TAG, "addMessageUpdateListener", "chatRoomId: $chatRoomId listenerName: $listenerName", ModuleNames.FIREBASE_API.value)

        val listenerRegistration = firestore?.collection(FirestoreCollections.CHAT_ROOMS.collectionName)
            ?.document(chatRoomId)
            ?.collection(FirestoreCollections.MESSAGES.collectionName)
            ?.orderBy(MessageDataFields.TIMESTAMP.fieldName, Direction.DESCENDING)
            ?.addSnapshotListener(listener)

        messageListListenerMap[listenerName] = listenerRegistration
    }

    @Throws(Exception::class)
    override fun removeMessageUpdateListener(listenerName: String) {
        Logger.d(TAG, "removeMessageUpdateListener", "listenerName: $listenerName", ModuleNames.FIREBASE_API.value)

        messageListListenerMap[listenerName]?.remove()
        messageListListenerMap.remove(listenerName)
    }

    companion object {
        private const val TAG = "FirebaseFirestoreHelper"

        private var helper: FirebaseFirestoreHelper? = null
        private val lock = Any()

        @Synchronized
        fun getInstance(): FirebaseFirestoreApi {
            synchronized(lock) {
                return helper ?: synchronized(lock) {
                    Logger.i(TAG, "getInstance", "instance is null, creating a new instance", ModuleNames.FIREBASE_API.value)

                    FirebaseFirestoreHelper().also {
                        Logger.i(TAG, "getInstance", "setting up the created instance", ModuleNames.FIREBASE_API.value)
                        helper = it
                    }
                }
            }
        }
    }
}