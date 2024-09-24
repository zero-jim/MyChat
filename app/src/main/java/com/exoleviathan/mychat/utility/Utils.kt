package com.exoleviathan.mychat.utility

import android.app.Activity
import android.view.inputmethod.InputMethodManager
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val AUTH_FRAGMENT_NAME = "auth_fragment_name"
const val MESSAGE_RECEIVER_NAME = "message_receiver_title"
const val MESSAGE_RECEIVER_UID = "message_receiver_uid"

const val FCM_MESSAGE_PREFERENCE_ID = "fcm_message_pref"
const val FCM_TOKEN_KEY = "fcm_token_key"

enum class ModuleNames(val value: String) {
    AUTH("[AUTH]"),
    HOME("[HOME]"),
    MESSAGE("[MESSAGE]"),
    FIREBASE_API("[FIREBASE_API]")
}

fun hideKeyboard(activity: Activity) {
    val ime = activity.getSystemService(InputMethodManager::class.java)
    ime.hideSoftInputFromWindow(activity.window.currentFocus?.windowToken, 0)
}

fun showKeyboard(activity: Activity) {
    val ime = activity.getSystemService(InputMethodManager::class.java)
    ime.showSoftInput(activity.window.currentFocus, 0)
}

fun Timestamp.toFormattedDate(): String = run {
    val date = this.toDate()
    val sdf = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
    return@run date.let<Date, String> { sdf.format(it) }
}