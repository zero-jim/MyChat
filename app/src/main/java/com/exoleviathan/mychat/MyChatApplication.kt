package com.exoleviathan.mychat

import android.app.Application
import com.exoleviathan.mychat.utility.Logger

class MyChatApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Logger.i(TAG, "onCreate")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Logger.i(TAG, "onLowMemory")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Logger.i(TAG, "onTrimMemory", "level: $level")
    }

    override fun onTerminate() {
        super.onTerminate()
        Logger.i(TAG, "onTerminate")
    }

    companion object {
        private const val TAG = "MyChatApplication"
    }
}