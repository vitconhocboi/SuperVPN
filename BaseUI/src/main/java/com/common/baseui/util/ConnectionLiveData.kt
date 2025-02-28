package com.common.baseui.util

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkInfo
import androidx.lifecycle.LiveData

class ConnectionLiveData(val context: Context) : LiveData<Boolean>() {

    private var connectivityManager =
        context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    private var networkCallback: NetworkCallback = NetworkCallback(this)

    override fun onActive() {
        super.onActive()
        updateConnection()
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    override fun onInactive() {
        super.onInactive()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    fun updateConnection() {
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        postValue(activeNetwork?.isConnectedOrConnecting == true)
    }

    class NetworkCallback(val liveData: ConnectionLiveData) :
        ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            liveData.postValue(true)
        }

        override fun onLost(network: Network) {
            liveData.postValue(false)
        }
    }
}