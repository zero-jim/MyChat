package com.exoleviathan.mychat.utility

import android.util.Log

object Logger {
    private const val APPLICATION_TAG = "[Application]"
    private const val ENTRY_MESSAGE = "enter"

    fun d(tag: String?, methodName: String?, message: String? = null, moduleName: String? = APPLICATION_TAG) {
        Log.d("${moduleName}_$tag", "$methodName() ${message ?: ENTRY_MESSAGE}")
    }

    fun i(tag: String?, methodName: String?, message: String? = null, moduleName: String? = APPLICATION_TAG) {
        Log.i("${moduleName}_$tag", "$methodName() ${message ?: ENTRY_MESSAGE}")
    }

    fun w(tag: String?, methodName: String?, message: String? = null, moduleName: String? = APPLICATION_TAG) {
        Log.w("${moduleName}_$tag", "$methodName() ${message ?: ENTRY_MESSAGE}")
    }

    fun e(tag: String?, methodName: String?, message: String? = null, moduleName: String? = APPLICATION_TAG) {
        Log.e("${moduleName}_$tag", "$methodName() ${message ?: ENTRY_MESSAGE}")
    }
}