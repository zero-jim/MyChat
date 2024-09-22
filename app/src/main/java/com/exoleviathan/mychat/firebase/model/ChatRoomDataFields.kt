package com.exoleviathan.mychat.firebase.model

enum class ChatRoomDataFields(val fieldName: String) {
    PARTICIPANT_NAMES("participant_names"),
    PARTICIPANT_LIST("participant_list"),
    LAST_MESSAGE("last_message"),
    LAST_MESSAGE_SENDER("last_message_sender"),
    TIMESTAMP("timestamp"),
    READ_STATUS("read_status")
}