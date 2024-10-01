package com.exoleviathan.mychat.common

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.exoleviathan.mychat.utility.Logger
import kotlinx.coroutines.flow.MutableStateFlow

abstract class MyChatActivity : AppCompatActivity() {
    val networkState = MutableStateFlow(true)

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Logger.i(TAG, "networkCallback::onAvailable")

            networkState.tryEmit(true)
        }

        override fun onUnavailable() {
            super.onUnavailable()
            Logger.i(TAG, "networkCallback::onUnavailable")

            networkState.tryEmit(false)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            Logger.i(TAG, "networkCallback::onLost")

            networkState.tryEmit(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate")

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR).build()

        val connectivityManager = this.getSystemService(ConnectivityManager::class.java)
        connectivityManager.requestNetwork(networkRequest, networkCallback)

        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
            networkState.tryEmit(true)
        } else if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true) {
            networkState.tryEmit(true)
        } else {
            networkState.tryEmit(true)
        }
    }

    companion object {
        private const val TAG = "MyChatActivity"
    }
}