package com.akmobile.supervpn.network

import android.content.Context
import android.util.Base64
import android.util.Log
import com.common.baseui.BaseAppConfig
import timber.log.Timber
import java.io.File

class PrivoxyManager(private val context: Context) {
    private var isRunning = false
    private var configPath: String = ""
    private var port: Int = 8118 // Default Privoxy port

    companion object {
        init {
            System.loadLibrary("privoxy-jni")
        }

        @JvmStatic
        private external fun nativeStartPrivoxy(configPath: String): Boolean

        @JvmStatic
        private external fun nativeStopPrivoxy(): Boolean

        @JvmStatic
        private external fun nativeIsRunning(): Boolean
    }

    fun initialize(): Boolean {
        try {
            // Create Privoxy configuration directory
            val privoxyDir = File(context.filesDir, "privoxy")
            if (!privoxyDir.exists()) {
                privoxyDir.mkdirs()
            }

            // Create config file
            configPath = createConfigFile(privoxyDir)
            return true
        } catch (e: Exception) {
            Timber.tag("PrivoxyManager").e(e, "Failed to initialize Privoxy")
            return false
        }
    }

    fun start(): Boolean {
        if (isRunning) return true

        val success = nativeStartPrivoxy(configPath)
        if (success) {
            isRunning = true
            Timber.tag("PrivoxyManager").d("Privoxy started successfully")
        } else {
            Timber.tag("PrivoxyManager").e("Failed to start Privoxy")
        }
        return success
    }

    fun stop(): Boolean {
        if (!isRunning) return true

        val success = nativeStopPrivoxy()
        if (success) {
            isRunning = false
            Timber.tag("PrivoxyManager").d("Privoxy stopped successfully")
        } else {
            Timber.tag("PrivoxyManager").e("Failed to stop Privoxy")
        }
        return success
    }

    fun isRunning(): Boolean {
        return nativeIsRunning()
    }

    fun getProxyAddress(): String {
        return "127.0.0.1:$port"
    }

    private fun createConfigFile(privoxyDir: File): String {
        val actionFile = File(privoxyDir, "default.action")
        val proxyType = BaseAppConfig.proxyType

        if (proxyType.lowercase() == "http") {
            val proxyUser = BaseAppConfig.proxyUser
            val proxyPass = BaseAppConfig.proxyPass
            actionFile.writeText(
                """
{+add-header{proxy-authorization: Basic ${Base64.encodeToString("$proxyUser:$proxyPass".toByteArray(),Base64.NO_WRAP)}}}
/
                """.replaceIndent()
            )
        }
        val configFile = File(privoxyDir, "config")
        configFile.writeText(
            """listen-address 127.0.0.1:$port
actionsfile ${actionFile.absolutePath}
${getForwardSettings()}
""".replaceIndent()
        )

        return configFile.absolutePath
    }

    private fun getForwardSettings(): String {
        // Get proxy settings from your existing configuration
        val proxyType = BaseAppConfig.proxyType
        val proxyHost = BaseAppConfig.proxyHost
        val proxyPort = BaseAppConfig.proxyPort
        val proxyUser = BaseAppConfig.proxyUser
        val proxyPass = BaseAppConfig.proxyPass

        if (proxyType.isNotEmpty() && proxyHost.isNotEmpty() && proxyPort.isNotEmpty()) {
            if (proxyType.lowercase() == "http") {
                return """forward / $proxyHost:$proxyPort
enable-proxy-authentication-forwarding 1
""".replaceIndent("")
            } else {
                return """forward-socks5 / $proxyUser:$proxyPass@$proxyHost:$proxyPort .
""".trimMargin().replaceIndent("")
            }
        } else {
            return "forward / ."  // Direct connection
        }
    }
} 