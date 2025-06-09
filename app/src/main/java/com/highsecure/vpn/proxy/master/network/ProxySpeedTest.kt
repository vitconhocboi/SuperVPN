package com.highsecure.vpn.proxy.master.network

import android.annotation.SuppressLint
import android.os.AsyncTask
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber
import java.net.Authenticator
import java.net.InetSocketAddress
import java.net.PasswordAuthentication
import java.net.Proxy
import java.util.concurrent.TimeUnit


class ProxySpeedTest {

    companion object {

        val Instance: ProxySpeedTest = ProxySpeedTest()

        private const val TAG = "Socks5SpeedTest"
        private const val DOWNLOAD_URL =
            "https://speed.cloudflare.com/__down?bytes=1000000" // 1MB test file
        private const val UPLOAD_URL =
            "https://speed.cloudflare.com/__up" // Test server for upload
        private const val UPLOAD_SIZE_BYTES = 1024 * 1024 // 1MB payload for upload
        const val FAILED = "Failed"
    }

    private var testProxyTask: TestProxyTask? = null

    // SOCKS5 proxy configuration with authentication
    data class ProxyConfig (
        val host: String,
        val port: Int,
        val username: String,
        val password: String,
        val type: String
    )

    // Test SOCKS5 proxy download and upload speeds
    fun startSpeedTest(proxyConfig: ProxyConfig?, callback: (download: String, upload: String) -> Unit) {
        testProxyTask = TestProxyTask(proxyConfig, callback)
        testProxyTask?.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun stopProxyTest() {
        testProxyTask?.cancel(true)
    }

    private class TestProxyTask(
        private val proxyConfig: ProxyConfig?,
        private val callback: (download: String, upload: String) -> Unit
    ) : AsyncTask<Void, Void, Pair<String, String>>() {

        @Deprecated("Deprecated in Java")
        override fun onPreExecute() {
            Timber.i("$TAG Starting SOCKS5 proxy test for $proxyConfig")
        }

        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: Void?): Pair<String, String> {
            Timber.i("$TAG doInBackground started")
            try {
                if (isCancelled()) {
                    return Pair("--", "--")
                }
                val client = buildOkHttpClient(proxyConfig)

                // Measure download speed
                val downloadSpeed = try {
                    measureDownloadSpeed(client)
                } catch (e: Exception) {
//                    Timber.i(e, "$TAG Download speed test failed: ${e.message}")
                    -1.0 // Indicate failure
                }

                // Measure upload speed
                val uploadSpeed = try {
                    measureUploadSpeed(client)
                } catch (e: Exception) {
//                    Timber.i(e, "$TAG Upload speed test failed: ${e.message}")
                    -1.0 // Indicate failure
                }

                return Pair(formatSpeed(downloadSpeed), formatSpeed(uploadSpeed))
            } catch (_: Exception) {
            }
            return Pair("--", "--")
        }

        @Deprecated("Deprecated in Java")
        override fun onPostExecute(result: Pair<String, String>) {
            callback(result.first, result.second)
        }

        @Deprecated("Deprecated in Java")
        override fun onCancelled() {
            callback("--", "--")
        }

        private fun buildOkHttpClient(proxyConfig: ProxyConfig?): OkHttpClient {
            val builder = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
            if (proxyConfig != null) {
                val proxy = Proxy(
                    when (proxyConfig.type.uppercase()) {
                        "SOCKS5" -> Proxy.Type.SOCKS
                        "HTTP" -> Proxy.Type.HTTP
                        else -> throw IllegalArgumentException("Unsupported proxy type: ${proxyConfig.type}")
                    },
                    InetSocketAddress(proxyConfig.host, proxyConfig.port)
                )
                builder.proxy(proxy)
            }
            if (proxyConfig?.type?.uppercase() == "HTTP") {
                builder.proxyAuthenticator(object : okhttp3.Authenticator {
                    override fun authenticate(route: Route?, response: Response): Request? {
                        val credential = Credentials.basic(
                            proxyConfig.username,
                            proxyConfig.password
                        )
                        return response.request.newBuilder()
                            .header("Proxy-Authorization", credential)
                            .build()
                    }
                })
            } else if (proxyConfig?.type?.uppercase() == "SOCKS5") {
                Authenticator.setDefault(object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(
                            proxyConfig.username,
                            proxyConfig.password.toCharArray()
                        )
                    }
                })
            }
            return builder.build()
        }

        private fun measureDownloadSpeed(client: OkHttpClient): Double {
            val request = Request.Builder().url(DOWNLOAD_URL).build()
            val startTime = System.currentTimeMillis()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("Download test failed with HTTP ${response.code}")
                }
                val body = response.body.byteStream()
                var bytesRead = 0L
                val buffer = ByteArray(1024)
                while (body.read(buffer).also { if (it != -1) bytesRead += it } != -1) {
                    if (isCancelled) throw Exception("Task cancelled during download test")
                }
                val timeTaken = (System.currentTimeMillis() - startTime) / 1000.0 // in seconds
                return if (timeTaken > 0) (bytesRead / 1024.0) / timeTaken else 0.0 // KB/s
            }
        }

        private fun measureUploadSpeed(client: OkHttpClient): Double {
            val payload =
                ByteArray(UPLOAD_SIZE_BYTES) { 'A'.code.toByte() } // 1MB of 'A' characters
            // Use toRequestBody with proper MediaType
            val requestBody = payload.toRequestBody("application/octet-stream".toMediaTypeOrNull())
            val request = Request.Builder()
                .url(UPLOAD_URL)
                .post(requestBody)
                .build()

            val startTime = System.currentTimeMillis()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("Upload test failed with HTTP ${response.code}")
                }
                val timeTaken = (System.currentTimeMillis() - startTime) / 1000.0 // in seconds
                return if (timeTaken > 0) (UPLOAD_SIZE_BYTES / 1024.0) / timeTaken else 0.0 // KB/s
            }
        }

        @SuppressLint("DefaultLocale")
        private fun formatSpeed(speedKBps: Double): String {
            if (speedKBps < 0) return FAILED
            // Convert KB/s (kilobytes/sec) to Kb/s (kilobits/sec)
            val speedKbps = speedKBps * 8
            // Round to 2 decimal places
            return if (speedKbps >= 1000) {
                // Convert to Mb/s if ≥ 1000 Kb/s
                val speedMbps = speedKbps / 1000
                String.format("%.2f Mb/s", speedMbps)
            } else {
                String.format("%.2f Kb/s", speedKbps)
            }
        }
    }
}