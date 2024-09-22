package com.exoleviathan.mychat.auth.models

import android.text.TextUtils

enum class AuthFragmentNames(val fragment: String) {
    LOGIN("login_fragment"),
    REGISTRATION("registration_fragment"),
    EMAIL_VERIFICATION("email_verification_fragment"),
    FORGOT_PASSWORD("forgot_password_fragment"),
    UNKNOWN("unknown_fragment");

    companion object {

        fun get(value: String?): AuthFragmentNames {
            entries.forEach {
                if (TextUtils.equals(it.fragment, value)) {
                    return it
                }
            }

            return UNKNOWN
        }
    }
}