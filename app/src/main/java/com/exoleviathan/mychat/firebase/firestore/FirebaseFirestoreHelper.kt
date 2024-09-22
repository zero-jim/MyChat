package com.exoleviathan.mychat.firebase.firestore

import android.text.TextUtils
import com.exoleviathan.mychat.firebase.auth.FirebaseAuthenticationHelper
import com.exoleviathan.mychat.firebase.model.ChatRoomData
import com.exoleviathan.mychat.firebase.model.ChatRoomDataFields
import com.exoleviathan.mychat.firebase.model.FirestoreCollections
import com.exoleviathan.mychat.firebase.model.MessageData
import com.exoleviathan.mychat.firebase.model.MessageDataFields
import com.exoleviathan.mychat.firebase.model.UserData
import com.exoleviathan.mychat.firebase.model.UserDataFields
import com.exoleviathan.mychat.message.model.MessageViewHolders
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query.Direction
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FirebaseFirestoreHelper {
    private var firestore: FirebaseFirestore? = null
    private val friendListUpdateListenerMap = hashMapOf<String, ListenerRegistration?>()
    private val messageListListenerMap = hashMapOf<String, ListenerRegistration?>()
    private val chatRoomListenerMap = hashMapOf<String, ListenerRegistration?>()

    private fun initialize() {
        firestore = Firebase.firestore
    }

    fun isUserProfileAlreadyCreated(uid: String, isUserProfileCreated: (Boolean) -> Unit) {
        val collection = firestore?.collection(FirestoreCollections.USER_PROFILE.collectionName)
        val query = collection?.whereEqualTo(UserDataFields.USER_ID.fieldName, uid)

        query?.get()?.addOnSuccessListener {
            if (it.isEmpty) {
                Logger.i(TAG, "isUserProfileAlreadyCreated", "profile is not created", ModuleNames.FIREBASE_API.value)
                isUserProfileCreated.invoke(false)
            } else {
                Logger.i(TAG, "isUserProfileAlreadyCreated", "profile is already created", ModuleNames.FIREBASE_API.value)
                isUserProfileCreated.invoke(true)
            }
        }?.addOnFailureListener {
            isUserProfileAlreadyCreated(uid, isUserProfileCreated)
        }
    }

    fun createUserProfile(data: UserData?, result: (Boolean, String, String?) -> Unit) {
        Logger.d(TAG, "createUserProfile", "data: $data", ModuleNames.FIREBASE_API.value)

        data?.let {
            Logger.i(TAG, "createUserProfile", "user data is available", ModuleNames.FIREBASE_API.value)
            val userProfile = hashMapOf(
                Pair(UserDataFields.USER_ID.fieldName, it.uid),
                Pair(UserDataFields.USER_NAME.fieldName, it.displayName),
                Pair(UserDataFields.PROFILE_PHOTO_URL.fieldName, it.photoUrl),
                Pair(UserDataFields.EMAIL.fieldName, it.email),
                Pair(UserDataFields.STATUS.fieldName, it.status),
                Pair(UserDataFields.CREATION_TIME.fieldName, FieldValue.serverTimestamp().toString()),
                Pair(UserDataFields.FRIEND_LIST.fieldName, FieldValue.arrayUnion()),
                Pair(UserDataFields.GROUP_LIST.fieldName, FieldValue.arrayUnion()),
            )

            firestore?.collection(FirestoreCollections.USER_PROFILE.collectionName)
                ?.document(it.uid)
                ?.set(userProfile)
                ?.addOnSuccessListener {
                    result(true, "", "")
                }?.addOnFailureListener { exp ->
                    result(false, "", exp.message)
                }
        }
    }

    private fun updateUserFriendList(uid: String, fieldValue: Pair<String?, String?>, isSuccess: (Boolean) -> Unit) {
        Logger.d(TAG, "updateUserFriendList", "uid: $uid fieldValue: $fieldValue", ModuleNames.FIREBASE_API.value)

        firestore?.collection(FirestoreCollections.USER_PROFILE.collectionName)
            ?.document(uid)
            ?.update(UserDataFields.FRIEND_LIST.fieldName, FieldValue.arrayUnion(fieldValue))
            ?.addOnSuccessListener {
                Logger.i(TAG, "updateUserFriendList", "update user friend list is successful", ModuleNames.FIREBASE_API.value)
                isSuccess.invoke(true)
            }?.addOnFailureListener {
                Logger.e(TAG, "updateUserFriendList", "update user friend list failed", ModuleNames.FIREBASE_API.value)
                isSuccess.invoke(false)
            }
    }

    fun addAsFriend(email: String, result: (Boolean, String) -> Unit) {
        Logger.d(TAG, "addAsFriend", "email: $email", ModuleNames.FIREBASE_API.value)

        val userInfo = FirebaseAuthenticationHelper.getInstance()?.getUserInformation()
        userInfo?.let { data ->
            val collection = firestore?.collection(FirestoreCollections.USER_PROFILE.collectionName)
            val query = collection?.whereEqualTo(UserDataFields.EMAIL.fieldName, email)

            query?.get()?.addOnSuccessListener {
                if (it.isEmpty) {
                    Logger.i(TAG, "addAsFriend", "user not found", ModuleNames.FIREBASE_API.value)
                    result.invoke(false, "There is no such user with this email address.")
                } else {
                    val friendUid = it.documents[0].data?.get(UserDataFields.USER_ID.fieldName) as? String
                    val friendName = it.documents[0].data?.get(UserDataFields.USER_NAME.fieldName) as? String
                    val userFriendData = Pair(friendUid, friendName)
                    Logger.i(TAG, "addAsFriend", "user found: $friendUid", ModuleNames.FIREBASE_API.value)

                    if (TextUtils.equals(data.uid, friendUid)) {
                        result.invoke(false, "This is the current user email address.")
                        return@addOnSuccessListener
                    }

                    friendUid?.let { _ ->
                        updateUserFriendList(data.uid, userFriendData) { res ->
                            if (res) {
                                result.invoke(true, "User added successfully.")
                            } else {
                                result.invoke(false, "Failed to add user to friend list.")
                            }
                        }
                    } ?: run {
                        Logger.e(TAG, "addAsFriend", "friend user id is null", ModuleNames.FIREBASE_API.value)
                        result.invoke(false, "No user found for this email address.")
                    }
                }
            }?.addOnFailureListener {
                result.invoke(false, "Failed to add new user, try again.")
            }
        } ?: run {
            Logger.i(TAG, "addAsFriend", "user information is not available", ModuleNames.FIREBASE_API.value)
            result.invoke(false, "Current user information is not available.")
        }
    }

    fun addFriendListUpdateListener(uid: String, listenerName: String, result: (ArrayList<Pair<String, String>>) -> Unit) {
        Logger.i(TAG, "addFriendListUpdateListener", moduleName = ModuleNames.FIREBASE_API.value)
        val listener = firestore?.collection(FirestoreCollections.USER_PROFILE.collectionName)
            ?.document(uid)
            ?.addSnapshotListener { value, _ ->
                val ret: ArrayList<Pair<String, String>> = arrayListOf()
                val friends = value?.data?.get(UserDataFields.FRIEND_LIST.fieldName) as? ArrayList<*>
                Logger.i(TAG, "addFriendListUpdateListener", "friend: $friends", ModuleNames.FIREBASE_API.value)

                friends?.forEach {  friend ->
                    val friendUid = (friend as? HashMap<*, *>)?.get("first") as? String
                    val friendName = (friend as? HashMap<*, *>)?.get("second") as? String
                    Logger.i(TAG, "addFriendListUpdateListener", "friendName: $friendName", ModuleNames.FIREBASE_API.value)

                    friendName?.let { name ->
                        friendUid?.let { uid ->
                            ret.add(Pair(uid, name))
                        }
                    }
                }

                result.invoke(ret)
            }

        friendListUpdateListenerMap[listenerName] = listener
    }

    fun removeFriendListUpdateListener(listenerName: String) {
        friendListUpdateListenerMap[listenerName]?.remove()
        friendListUpdateListenerMap.remove(listenerName)
    }

    private fun updateReadStatus(chatRoomId: String) {
        Logger.i(TAG, "updateReadStatus", "chatRoomId: $chatRoomId", ModuleNames.FIREBASE_API.value)

        firestore?.collection(FirestoreCollections.CHAT_ROOMS.collectionName)
            ?.document(chatRoomId)
            ?.update(ChatRoomDataFields.READ_STATUS.fieldName, true)
    }

    fun sendMessage(senderId: String, senderName: String, receiverId: String, receiverName: String, message: String, isSuccess: (Boolean, String) -> Unit) {
        val chatRoomId = arrayOf(senderId, receiverId).sortedArray().joinToString(separator = "_")
        Logger.i(TAG, "sendMessage", "chatRoomId: $chatRoomId", ModuleNames.FIREBASE_API.value)

        FirebaseAuthenticationHelper.getInstance()?.getUserInformation()?.let { userData ->
            userData.displayName?.let { name ->
                val data = hashMapOf(
                    Pair(ChatRoomDataFields.PARTICIPANT_NAMES.fieldName, listOf(senderName, receiverName).sorted()),
                    Pair(ChatRoomDataFields.PARTICIPANT_LIST.fieldName, listOf(senderId, receiverId).sorted()),
                    Pair(ChatRoomDataFields.LAST_MESSAGE.fieldName, message),
                    Pair(ChatRoomDataFields.LAST_MESSAGE_SENDER.fieldName, name),
                    Pair(ChatRoomDataFields.TIMESTAMP.fieldName, FieldValue.serverTimestamp()),
                    Pair(ChatRoomDataFields.READ_STATUS.fieldName, false)
                )

                firestore?.collection(FirestoreCollections.CHAT_ROOMS.collectionName)
                    ?.document(chatRoomId)
                    ?.set(data)
                    ?.addOnSuccessListener {
                        val messageData = hashMapOf(
                            Pair(MessageDataFields.MESSAGE.fieldName, message),
                            Pair(MessageDataFields.SENDER_ID.fieldName, senderId),
                            Pair(MessageDataFields.TIMESTAMP.fieldName, FieldValue.serverTimestamp())
                        )

                        firestore?.collection(FirestoreCollections.CHAT_ROOMS.collectionName)
                            ?.document(chatRoomId)
                            ?.collection(FirestoreCollections.MESSAGES.collectionName)
                            ?.add(messageData)
                            ?.addOnSuccessListener {
                                isSuccess.invoke(true, "Message sent successfully.")
                            }?.addOnFailureListener {
                                isSuccess.invoke(false, "Message sending failed.")
                            }
                    }?.addOnFailureListener {
                        isSuccess.invoke(false, "Message sending failed.")
                    }
            } ?: run {
                Logger.w(TAG, "sendMessage", "user name is not available", ModuleNames.FIREBASE_API.value)
                isSuccess.invoke(false, "User name is not available.")
            }
        } ?: run {
            Logger.w(TAG, "sendMessage", "user information is not available", ModuleNames.FIREBASE_API.value)
            isSuccess.invoke(false, "User information is not available.")
        }
    }

    fun addMessageSnapshotListener(senderId: String, receiverId: String, result: (ArrayList<MessageData>) -> Unit) {
        val chatRoomId = arrayOf(senderId, receiverId).sortedArray().joinToString(separator = "_")
        Logger.i(TAG, "addMessageSnapshotListener", "chatRoomId: $chatRoomId", ModuleNames.FIREBASE_API.value)

        val listener = firestore?.collection(FirestoreCollections.CHAT_ROOMS.collectionName)
            ?.document(chatRoomId)
            ?.collection(FirestoreCollections.MESSAGES.collectionName)
            ?.orderBy(MessageDataFields.TIMESTAMP.fieldName, Direction.DESCENDING)
            ?.addSnapshotListener { querySnapshots, _ ->
                CoroutineScope(Dispatchers.Default).launch {
                    updateReadStatus(chatRoomId)
                }

                Logger.i(TAG, "addMessageSnapshotListener", "querySnapshots: ${querySnapshots?.documentChanges?.size}", ModuleNames.FIREBASE_API.value)
                if (querySnapshots?.documentChanges?.size == 0) {
                    result.invoke(arrayListOf())
                }

                querySnapshots?.documentChanges?.forEach {
                    if (it.type == DocumentChange.Type.ADDED || it.type == DocumentChange.Type.REMOVED) {
                        val messageList = arrayListOf<MessageData>()
                        querySnapshots.documents.distinct().forEach { ds ->
                            val message = ds.get(MessageDataFields.MESSAGE.fieldName) as? String
                            val msgSenderId = ds.get(MessageDataFields.SENDER_ID.fieldName) as? String
                            val timeStamp = ds.getTimestamp(MessageDataFields.TIMESTAMP.fieldName)

                            messageList.add(
                                MessageData(
                                    message,
                                    if (TextUtils.equals(msgSenderId, senderId)) MessageViewHolders.SELF.value else MessageViewHolders.OTHER.value,
                                    timeStamp
                                )
                            )
                        }
                        result.invoke(messageList)
                    } else {
                        Logger.w(TAG, "addMessageSnapshotListener", "querySnapshots: ${it.type}", ModuleNames.FIREBASE_API.value)
                    }
                } ?: run {
                    result.invoke(arrayListOf())
                }
            }

        messageListListenerMap[chatRoomId] = listener
    }

    fun removeMessageSnapshotListener(senderId: String, receiverId: String) {
        val messageId = arrayOf(senderId, receiverId).sortedArray().joinToString(separator = "_")
        Logger.i(TAG, "removeMessageSnapshotListener", "messageId: $messageId", ModuleNames.FIREBASE_API.value)

        messageListListenerMap[messageId]?.remove()
        messageListListenerMap.remove(messageId)
    }

    fun addChatRoomListener(currentUserId: String, result: (ArrayList<ChatRoomData>) -> Unit) {
        Logger.i(TAG, "addChatRoomListener", "currentUserId: $currentUserId", ModuleNames.FIREBASE_API.value)

        val listener = firestore?.collection(FirestoreCollections.CHAT_ROOMS.collectionName)
            ?.whereArrayContains(ChatRoomDataFields.PARTICIPANT_LIST.fieldName, currentUserId)
            ?.orderBy(ChatRoomDataFields.TIMESTAMP.fieldName)
            ?.addSnapshotListener { querySnapshot, _ ->
                Logger.i(TAG, "addChatRoomListener", "querySnapshot: ${querySnapshot?.documentChanges?.size}", ModuleNames.FIREBASE_API.value)
                if (querySnapshot?.documentChanges?.size == 0) {
                    result.invoke(arrayListOf())
                }

                querySnapshot?.documentChanges?.forEach {
                    Logger.i(TAG, "addChatRoomListener", "querySnapshots: ${it.type}", ModuleNames.FIREBASE_API.value)
                    val chatRooms = arrayListOf<ChatRoomData>()

                    querySnapshot.documents.forEach { ds ->
                        val participantsList = ds.get(ChatRoomDataFields.PARTICIPANT_LIST.fieldName) as? ArrayList<*>
                        val lastMessage = ds.get(ChatRoomDataFields.LAST_MESSAGE.fieldName) as? String
                        val lastMessageSender = ds.get(ChatRoomDataFields.LAST_MESSAGE_SENDER.fieldName) as? String
                        val lastMessageTime = ds.getTimestamp(ChatRoomDataFields.TIMESTAMP.fieldName)
                        val participantNames = ds.get(ChatRoomDataFields.PARTICIPANT_NAMES.fieldName) as? ArrayList<*>
                        val readStatus = ds.get(ChatRoomDataFields.READ_STATUS.fieldName) as? Boolean

                        val chatRoom = ChatRoomData(
                            participantsList,
                            lastMessage,
                            lastMessageSender,
                            lastMessageTime,
                            participantNames,
                            readStatus ?: false
                        )
                        Logger.d(TAG, "addChatRoomListener", "chatRoom: $chatRoom", ModuleNames.FIREBASE_API.value)
                        chatRooms.add(chatRoom)
                    }

                    result.invoke(chatRooms)
                }
            }

        chatRoomListenerMap[currentUserId] = listener
    }

    fun removeChatRoomListener(currentUserId: String) {
        chatRoomListenerMap[currentUserId]?.remove()
        chatRoomListenerMap.remove(currentUserId)
    }

    companion object {
        private const val TAG = "FirebaseFirestoreHelper"
        private var helper: FirebaseFirestoreHelper? = null
        private val lock = Any()

        @Synchronized
        fun getInstance(): FirebaseFirestoreHelper? {
            synchronized(lock) {
                return helper ?: synchronized(lock) {
                    helper = FirebaseFirestoreHelper()
                    helper?.initialize()
                    return helper
                }
            }
        }
    }
}