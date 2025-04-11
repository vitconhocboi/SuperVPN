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
    private val UPLOAD: Int = 0
    private val DOWNLOAD: Int = 1

    companion object {
        init {
            System.loadLibrary("privoxy-jni")
        }

        @JvmStatic
        private external fun nativeStartPrivoxy(configPath: String, owner: PrivoxyManager): Boolean

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

    fun saveToPref(type: Int, value: Int) {
        if (type == DOWNLOAD) {
            var totalDownload =
                context.getSharedPreferences("privoxy_traffic", Context.MODE_PRIVATE)
                    .getInt("download", 0)
            context.getSharedPreferences("privoxy_traffic", Context.MODE_PRIVATE).edit()
                .putInt("download", totalDownload + value).apply()
        } else {
            var totalDownload =
                context.getSharedPreferences("privoxy_traffic", Context.MODE_PRIVATE)
                    .getInt("upload", 0)
            context.getSharedPreferences("privoxy_traffic", Context.MODE_PRIVATE).edit()
                .putInt("upload", totalDownload + value).apply()
        }
    }

    fun start(): Boolean {
        if (isRunning) return true

        val success = nativeStartPrivoxy(configPath, this@PrivoxyManager)
        if (success) {
            isRunning = true
            context.getSharedPreferences("privoxy_traffic", Context.MODE_PRIVATE).edit()
                .putInt("upload", 0).apply()
            context.getSharedPreferences("privoxy_traffic", Context.MODE_PRIVATE).edit()
                .putInt("download", 0).apply()
            context.getSharedPreferences("privoxy_traffic", Context.MODE_PRIVATE).edit()
                .putInt("start", System.currentTimeMillis().toInt() / 1000).apply()
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

        return true
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
{+add-header{proxy-authorization: Basic ${
                    Base64.encodeToString(
                        "$proxyUser:$proxyPass".toByteArray(), Base64.NO_WRAP
                    )
                }}}
/

{+block{No nasty stuff for you.}}
.nasty-stuff.example.com

{+block{Doubleclick banners.} +handle-as-image}
*.doubleclick.net
.doubleclick.net
ads.pubmatic.com
ads.betweendigital.com
ads.stickyadstv.com
*adsystem.com
*.smartadserver.com
*.googlesyndication.com
beacons.gcp.gvt2.com
beacons.gvt2.com
pix.pubpowerplatform.io
*.adnxs.com
*.creativecdn.com
*.unrulymedia.com
*.pubmatic.com
*.richaudience.com
*.aralego.com
prebid.*
                """.replaceIndent()
            )
        }
        val configFile = File(privoxyDir, "config")
        configFile.writeText(
            """listen-address 127.0.0.1:$port
                debug 66048
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

//        if (proxyType.isNotEmpty() && proxyHost.isNotEmpty() && proxyPort.isNotEmpty()) {
//            if (proxyType.lowercase() == "http") {
//                return """forward / $proxyHost:$proxyPort
//enable-proxy-authentication-forwarding 1
//""".replaceIndent("")
//            } else {
//                return """forward-socks5 / $proxyUser:$proxyPass@$proxyHost:$proxyPort .
//""".trimMargin().replaceIndent("")
//            }
//        } else {
            return "forward / ."  // Direct connection
//        }
    }
} 