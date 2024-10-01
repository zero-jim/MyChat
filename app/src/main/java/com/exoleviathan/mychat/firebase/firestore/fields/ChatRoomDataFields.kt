package com.exoleviathan.mychat.firebase.firestore.fields

enum class ChatRoomDataFields(val fieldName: String) {
    CHAT_ROOM_ID("chatRoomId"),
    TIMESTAMP("timestamp"),
    PARTICIPANT_ID_LIST("participantIdList"),
    READ_STATUS("readStatus")
}