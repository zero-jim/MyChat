package com.exoleviathan.mychat.utility

import android.content.Context
import android.content.Intent

object NavigationHelper {

    fun navigateToActivity(context: Context?, intent: Intent?) {
        context?.startActivity(intent)
    }
}