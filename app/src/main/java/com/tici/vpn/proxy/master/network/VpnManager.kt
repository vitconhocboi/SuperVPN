package com.tici.vpn.proxy.master.network

import android.content.Context
import android.util.Base64
import com.tici.vpn.proxy.master.utils.Constant
import com.common.baseui.BaseAppConfig
import timber.log.Timber
import java.io.File
import androidx.core.content.edit

class VpnManager(private val context: Context) {
    private var isRunning = false
    private var configPath: String = ""
    private var port: Int = 8118 // Default Privoxy port
    private val UPLOAD: Int = 0
    private val DOWNLOAD: Int = 1
    var currentProxy: ProxySpeedTest.ProxyConfig? = null

    companion object {
        init {
            System.loadLibrary("share-pr")
        }

        @JvmStatic
        private external fun nativeStart(configPath: String, owner: VpnManager): Boolean

        @JvmStatic
        private external fun nativeStop(): Boolean

        @JvmStatic
        private external fun nativeIsRunning(): Boolean
    }

    fun initialize(proxy: ProxySpeedTest.ProxyConfig?): Boolean {
        try {
            // Create Privoxy configuration directory
            val privoxyDir = File(context.filesDir, "privoxy")
            if (!privoxyDir.exists()) {
                privoxyDir.mkdirs()
            }
            currentProxy = proxy
            // Create config file
            configPath = createConfigFile(privoxyDir)
            return true
        } catch (e: Exception) {
            Timber.tag("PrivoxyManager").e(e, "Failed to initialize Privoxy")
            return false
        }
    }

//    fun saveToPref(type: Int, value: Int) {
//        if (type == DOWNLOAD) {
//            var totalDownload =
//                context.getSharedPreferences("privoxy_traffic", Context.MODE_PRIVATE)
//                    .getInt("download", 0)
//            context.getSharedPreferences("privoxy_traffic", Context.MODE_PRIVATE).edit() {
//                putInt("download", totalDownload + value)
//            }
//        } else {
//            var totalDownload =
//                context.getSharedPreferences("privoxy_traffic", Context.MODE_PRIVATE)
//                    .getInt("upload", 0)
//            context.getSharedPreferences("privoxy_traffic", Context.MODE_PRIVATE).edit() {
//                putInt("upload", totalDownload + value)
//            }
//        }
//    }

    fun start(): Boolean {
        if (isRunning) return true

        val success = nativeStart(configPath, this@VpnManager)
        if (success) {
            isRunning = true
            context.getSharedPreferences("privoxy_traffic", Context.MODE_PRIVATE).edit() {
                putInt("upload", 0)
            }
            context.getSharedPreferences("privoxy_traffic", Context.MODE_PRIVATE).edit() {
                putInt("download", 0)
            }
            context.getSharedPreferences("privoxy_traffic", Context.MODE_PRIVATE).edit() {
                putInt("start", System.currentTimeMillis().toInt() / 1000)
            }
            Timber.tag("PrivoxyManager").d("Privoxy started successfully")
        } else {
            Timber.tag("PrivoxyManager").e("Failed to start Privoxy")
        }
        return success
    }

    fun stop(): Boolean {
        if (!isRunning) return true
        val success = nativeStop()
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
        Timber.tag(Constant.TAG).d("createConfigFile: $actionFile")
        if (BaseAppConfig.adsBlock) {
            actionFile.writeText(
                """
{+block{Doubleclick banners.} +handle-as-image}
*.doubleclick.net
.doubleclick.net
ads.pubmatic.com
ads.betweendigital.com
ads.stickyadstv.com
*adsystem.com
*.smartadserver.com
*.googlesyndication.com
pix.pubpowerplatform.io
*.adnxs.com
*.creativecdn.com
*.unrulymedia.com
*.pubmatic.com
*.richaudience.com
*.aralego.com
*.googleadservices.com
.googleadservices.com
prebid.*


""".replaceIndent()
            )
        } else {
            actionFile.writeText(
                """
""".replaceIndent()
            )
        }

        if (currentProxy?.type?.lowercase() == "http") {
            actionFile.appendText(
                """
{+add-header{proxy-authorization: Basic ${
                    Base64.encodeToString(
                        "${currentProxy!!.username}:${currentProxy!!.password}".toByteArray(),
                        Base64.NO_WRAP
                    )
                }}}
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
//        Timber.tag(Constant.TAG).d("${currentProxy?.type}://${currentProxy!!.username}:${currentProxy!!.password}@${currentProxy!!.host}:${currentProxy!!.port}")
        if (currentProxy != null) {
            if (currentProxy?.type?.lowercase() == "http") {
                """forward / ${currentProxy!!.host}:${currentProxy!!.port}
    enable-proxy-authentication-forwarding 1
    """.replaceIndent("")
            } else {
                """forward-socks5 / ${currentProxy!!.username}:${currentProxy!!.password}@${currentProxy!!.host}:${currentProxy!!.port} .
    """.trimMargin().replaceIndent("")
            }
        } else {
            "forward / ."  // Direct connection
        }
    }
} 
