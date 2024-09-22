package com.exoleviathan.mychat

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.exoleviathan.mychat.utility.Logger

class MyChatApplication : Application() {

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Logger.i(TAG, "networkCallback::onAvailable")
        }

        override fun onUnavailable() {
            super.onUnavailable()
            Logger.i(TAG, "networkCallback::onUnavailable")
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            Logger.i(TAG, "networkCallback::onLost")
        }
    }

    override fun onCreate() {
        super.onCreate()
        Logger.i(TAG, "onCreate")

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR).build()

        val connectivityManager = this.getSystemService(ConnectivityManager::class.java)
        connectivityManager.requestNetwork(networkRequest, networkCallback)
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