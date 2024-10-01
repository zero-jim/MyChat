package com.exoleviathan.mychat.utility

import android.content.Context

class SharedPreferenceHelper(context: Context, prefId: String) {
    private val sharedPreferences = context.getSharedPreferences(prefId, Context.MODE_PRIVATE)

    fun <T> putItem(key: String, value: T): Boolean {
        var isSuccess: Boolean

        sharedPreferences.edit().apply {
            when (value) {
                is Boolean -> {
                    putBoolean(key, value)
                    isSuccess = true
                }

                is Int -> {
                    putInt(key, value)
                    isSuccess = true
                }

                is Long -> {
                    putLong(key, value)
                    isSuccess = true
                }

                is String -> {
                    putString(key, value)
                    isSuccess = true
                }

                else -> {
                    isSuccess = false
                }
            }
        }.apply()

        return isSuccess
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getItem(key: String, defValue: T): T {
        return when (defValue) {
            is Boolean -> {
                sharedPreferences.getBoolean(key, false) as? T ?: defValue
            }

            is Int -> {
                sharedPreferences.getInt(key, -1) as? T ?: defValue
            }

            is Long -> {
                sharedPreferences.getLong(key, -1L) as? T ?: defValue
            }

            is String -> {
                sharedPreferences.getString(key, null) as? T ?: defValue
            }

            else -> {
                defValue
            }
        }
    }
}