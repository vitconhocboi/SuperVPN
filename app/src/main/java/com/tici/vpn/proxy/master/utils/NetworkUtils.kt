package com.tici.vpn.proxy.master.utils

import android.content.Context
import android.net.ConnectivityManager
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
        var result = false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        cm?.run {
            cm.getNetworkCapabilities(cm.activeNetwork)
                ?.run {
                    if (when {

                            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                            hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                            hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
                            else -> false
                        }
                    ) {
                        result = hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    }
                }
        }
        return result
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
