package com.exoleviathan.mychat.utility

import android.content.Context
import android.content.Intent
import androidx.navigation.NavOptions
import androidx.navigation.navOptions

object NavigationHelper {

    fun navigateToActivity(context: Context?, intent: Intent?) {
        context?.startActivity(intent)
    }
}