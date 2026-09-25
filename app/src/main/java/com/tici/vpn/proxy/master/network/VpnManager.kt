package com.tici.vpn.proxy.master.network

import android.content.Context
import com.tici.vpn.proxy.master.utils.Constant
import timber.log.Timber
import java.io.File
import androidx.core.content.edit

class VpnManager(private val context: Context) {
    private var isRunning = false
    private var configPath: String = ""
    private var port: Int = 8118 // Default Privoxy port
    private val UPLOAD: Int = 0
    private val DOWNLOAD: Int = 1

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

    /**
     * Writes Privoxy's files. [actionFileBody] is pre-rendered and pre-validated by
     * `AdsBlockRepository.renderActionFile()`; this class knows nothing about rules or settings.
     */
    fun initialize(actionFileBody: String): Boolean {
        try {
            // Create Privoxy configuration directory
            val privoxyDir = File(context.filesDir, "privoxy")
            if (!privoxyDir.exists()) {
                privoxyDir.mkdirs()
            }
            // Create config file
            configPath = createConfigFile(privoxyDir, actionFileBody)
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

    private fun createConfigFile(privoxyDir: File, actionFileBody: String): String {
        val actionFile = PrivoxyActionFile.file(context)
        Timber.tag(Constant.TAG).d("createConfigFile: $actionFile")
        PrivoxyActionFile.write(actionFile, actionFileBody)

        val configFile = File(privoxyDir, "config")
        configFile.writeText(
            """listen-address 127.0.0.1:$port
debug 1024
actionsfile ${actionFile.absolutePath}
${getForwardSettings()}
""".replaceIndent()
        )

        return configFile.absolutePath
    }

    /**
     * Privoxy runs standalone: it filters locally and dials destinations itself.
     * No upstream proxy server, so forwarding is always direct.
     */
    private fun getForwardSettings(): String {
        return "forward / ."
    }
}
 
