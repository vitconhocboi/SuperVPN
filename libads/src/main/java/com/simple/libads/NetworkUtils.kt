package com.simple.libads

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.StrictMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.*

object NetworkUtils {
    @Suppress("DEPRECATION")
    fun isInternetAvailable(context: Context): Boolean {
        var result = false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        cm?.run {
            cm.getNetworkCapabilities(cm.activeNetwork)
                ?.run {
                    result = when {
                        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
                        else -> false
                    }
                }
        }
        return result
    }

    private fun hasInternetAccess(context: Context): Boolean {
        return if (isInternetAvailable(context)) {
            try {
                val timeout = 1500
                val executor: ExecutorService = Executors.newCachedThreadPool()
                val task: Callable<Boolean> = Callable<Boolean> {
                    val policy: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder()
                        .permitAll()
                        .build()
                    StrictMode.setThreadPolicy(policy)

                    val httpURLConnection: HttpURLConnection = URL("https://www.google.com").openConnection() as HttpURLConnection
                    httpURLConnection.setRequestProperty("User-Agent", "Android")
                    httpURLConnection.setRequestProperty("Connection", "close")
                    httpURLConnection.requestMethod = "GET"
                    httpURLConnection.connectTimeout = timeout
                    httpURLConnection.readTimeout = timeout
                    httpURLConnection.connect()
                    httpURLConnection.responseCode == 200
                }
                val future: Future<Boolean> = executor.submit(task)
                return future.get(timeout.toLong(), TimeUnit.MILLISECONDS)
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }

    fun hasInternetAccessCheck(doTask: () -> Unit, doException: () -> Unit) {
        launchIO {
            when {
                hasInternetAccess(AdsApplication.appContext) -> launchMain(doTask)
                else -> launchMain(doException)
            }
        }
    }

    private fun <T>launchIO(doWork: suspend CoroutineScope.() -> T): Job {
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
