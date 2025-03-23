package com.akmobile.supervpn.network

import android.content.Context
import android.util.Log
import com.common.baseui.BaseAppConfig
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
            Log.e("PrivoxyManager", "Failed to initialize Privoxy", e)
            return false
        }
    }

    fun start(): Boolean {
        if (isRunning) return true

        val success = nativeStartPrivoxy(configPath)
        if (success) {
            isRunning = true
            Log.d("PrivoxyManager", "Privoxy started successfully")
        } else {
            Log.e("PrivoxyManager", "Failed to start Privoxy")
        }
        return success
    }

    fun stop(): Boolean {
        if (!isRunning) return true

        val success = nativeStopPrivoxy()
        if (success) {
            isRunning = false
            Log.d("PrivoxyManager", "Privoxy stopped successfully")
        } else {
            Log.e("PrivoxyManager", "Failed to stop Privoxy")
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
        actionFile.writeText("""
        {+add-header{proxy-authentication: Basic VVM0NDM3NjQ6b1FGVzlyMnA=}}
        /
        """.trimIndent())
        val configFile = File(privoxyDir, "config")
        configFile.writeText(
            """
            listen-address 127.0.0.1:$port
            actionsfile ${actionFile.absolutePath}
            enable-proxy-authentication-forwarding 1
            # Forward settings
            ${getForwardSettings()}
        """.trimIndent()
        )

        return configFile.absolutePath
    }

    private fun getForwardSettings(): String {
        // Get proxy settings from your existing configuration
        val proxyAddress = BaseAppConfig.proxyAddress

        return if (proxyAddress != null && proxyAddress.isNotEmpty()) {
            """
            forward / 5.181.164.131:56789
            enable-proxy-authentication-forwarding 1
            """.trimMargin()
        } else {
            "forward / ."  // Direct connection
        }
    }
} 