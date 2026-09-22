package com.tici.vpn.proxy.master.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.StrictMode
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket

object NetworkUtils {
    @Suppress("DEPRECATION")
    fun isInternetAvailable(context: Context): Boolean {
        val cm = context.applicationContext
            .getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        return try {
            // activeNetwork can be null transiently (e.g. while the VPN tunnel is being
            // established or torn down), so fall back to scanning every known network.
            if (hasInternetCapability(cm, cm.activeNetwork)) return true
            if (cm.allNetworks.any { hasInternetCapability(cm, it) }) return true
            // Last resort for OEMs where NetworkCapabilities is unreliable.
            cm.activeNetworkInfo?.isConnected == true
        } catch (e: Exception) {
            Log.d("NetworkUtils", "Error checking network state", e)
            // Don't block the user because of a platform failure.
            true
        }
    }

    private fun hasInternetCapability(cm: ConnectivityManager, network: Network?): Boolean {
        val caps = network?.let { cm.getNetworkCapabilities(it) } ?: return false
        val hasTransport = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        return hasTransport && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun hasInternetAccess(context: Context): Boolean {
        return if (isInternetAvailable(context)) {
            // First check google.com connectivity
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            try {
                val sock = Socket()
                sock.connect(InetSocketAddress("www.google.com", 80), 1500)
                val output = PrintWriter(sock.getOutputStream(), true)
                output.println("HEAD / HTTP/1.0")
                output.flush()
                output.close()
                sock.close()
                true
            } catch (e: IOException) {
                Log.d("NetworkUtils", "Error checking internet connection", e)
                false
            }
        } else {
            false
        }
    }

    private fun <T> launchIO(doWork: suspend CoroutineScope.() -> T): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            doWork.invoke(this)
        }
    }

    private fun launchMain(doWork: () -> Unit): Job {
        return CoroutineScope(Dispatchers.Main).launch {
            doWork.invoke()
        }
    }

}
