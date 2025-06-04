package com.highsecure.vpn.proxy.master.network

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.ProxyInfo
import android.net.VpnService
import android.os.Binder
import android.os.Build
import android.os.DeadObjectException
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.system.Os
import androidx.core.app.NotificationCompat
import com.highsecure.vpn.proxy.master.R
import com.highsecure.vpn.proxy.master.home.HomeFragment
import com.highsecure.vpn.proxy.master.main.MainActivity
import com.highsecure.vpn.proxy.master.proxy.ProxyConnection
import com.highsecure.vpn.proxy.master.utils.Constant
import com.common.baseui.BaseAppConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import timber.log.Timber
import java.io.FileDescriptor
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

@AndroidEntryPoint
class LocalVpnService : VpnService(), Runnable {
//    private val m_Packet: ByteArray
//    private val m_IPHeader: IPHeader
    private var m_VPNThread: Thread? = null
    private var m_VPNInterface: ParcelFileDescriptor? = null
    private var m_PrivoxyManager: PrivoxyManager? = null

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    @Inject
    lateinit var proxyConnection: ProxyConnection
    var currentProxy: ProxySpeedTest.ProxyConfig? = null
    private val vpnInterface = AtomicReference<FileDescriptor?>(null)

    init {
        ID++
        if (Instance == null) {
            Instance = this
        }
    }

    override fun onCreate() {
        try {
            Timber.tag(Constant.TAG).d("LocalTcpServer started.")
        } catch (e: Exception) {
            Timber.tag(Constant.TAG).d("Failed to start TCP/DNS Proxy")
        }

        super.onCreate()
    }

    fun buildNotification(): Notification {
        val notificationChannelId = "vpn_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                "VPN Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("VPN is active")
            .setSmallIcon(R.drawable.ic_wifi)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(Notification.CATEGORY_SERVICE)

        return notificationBuilder.build()
    }

    private fun createNotificationChannel() {
        val channel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                "VPN_CHANNEL",
                "VPN Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent.getStringArrayListExtra("allowApp")?.isNotEmpty() == true) {
            allowApp = intent.getStringArrayListExtra("allowApp")
        }

        when (intent.action) {
            ACTION_START -> {
                IsRunning = true
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    createNotificationChannel()
                    startForeground(1, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
                }
                // Start a new session by creating a new thread.
                m_VPNThread = Thread(this, "VPNServiceThread")
                m_VPNThread!!.start()

                engine.Engine.decodeString(BaseAppConfig.proxy).split(":").let { parts ->
                    if (parts.size >= 5) {
                        currentProxy = ProxySpeedTest.ProxyConfig(
                            host = parts[1],
                            port = parts[2].toInt(),
                            username = parts.getOrNull(3) ?: "",
                            password = parts.getOrNull(4) ?: "",
                            type = parts.getOrNull(0) ?: "http"
                        )
                    } else {
                        currentProxy = null
                    }
                }

                m_PrivoxyManager = PrivoxyManager(this)
                if (m_PrivoxyManager!!.initialize(currentProxy) && m_PrivoxyManager!!.start()) {
                    Timber.tag(Constant.TAG)
                        .d("Privoxy started on: ${m_PrivoxyManager!!.getProxyAddress()}")
                    proxyConnection.updateUI(HomeFragment.CONNECTED)
                } else {
                    Timber.tag(Constant.TAG).d("Failed to start Privoxy")
                    proxyConnection.updateUI(HomeFragment.DISCONNECTED)
                }
            }

            ACTION_STOP -> {
                Timber.tag(Constant.TAG).d("Stop super vpn service")
                stopVPN()
            }
        }

        return START_NOT_STICKY
    }

    fun stopVPN() {
        if (IsRunning) {
            IsRunning = false
            // First stop the VPN thread to prevent new operations
            if (m_VPNThread != null) {
                m_VPNThread!!.interrupt()
                try {
                    m_VPNThread!!.join(1000) // Wait up to 1 second for thread to finish
                } catch (e: InterruptedException) {
                    Timber.tag(Constant.TAG).d("VPN thread interrupt error")
                }
                m_VPNThread = null
            }

            // Detach file descriptor before stopping engine
            if (m_VPNInterface != null) {
                try {
                    m_VPNInterface!!.close()
                    Timber.tag(Constant.TAG).d("Successfully detached fd:")
                } catch (e: Exception) {
                    Timber.tag(Constant.TAG)
                        .d("Error detaching VPN interface fd ${e.printStackTrace()}")
                }
                m_VPNInterface = null
            }

            // Now stop engine after fd is detached
            if (BaseAppConfig.proxy.isNotEmpty()) {
                engine.Engine.stop()
            }
            // Stop other components
            m_PrivoxyManager?.stop()
            m_PrivoxyManager = null
            currentProxy = null

            Instance!!.stopForeground(true)
            Instance!!.stopSelf() // Stop the service after cleanup
            Instance = null // Clear the instance reference

            Timber.tag(Constant.TAG).d("VPNService stopped.")
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return LocalBinder(this)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        // Ensure clean unbinding
        try {
            if (IsRunning) {
                // If still running, initiate cleanup
                stopVPN()
            }
        } catch (e: DeadObjectException) {
            Timber.tag(Constant.TAG).e("Error during service unbind ${e.printStackTrace()}")
        }
        return false
    }

    class LocalBinder(val service: LocalVpnService) : Binder()

    override fun onRevoke() {
        super.onRevoke()
        stopVPN()
    }

    private fun startTunToSock(pfdDescriptor: ParcelFileDescriptor? = null) {
        val key = engine.Key()
        key.mark = 0
        key.mtu = ProxyConfig.Instance.mTU.toLong()
        key.device = "fd://" + pfdDescriptor?.fd
        key.logLevel = "silent"
        if (currentProxy?.type?.lowercase() == "socks5") {
            key.proxy =
                "socks5://${currentProxy!!.username}:${currentProxy!!.password}@${currentProxy!!.host}:${currentProxy!!.port}"
        } else {
            key.proxy = "http://127.0.0.1:8118"
        }
        engine.Engine.insert(key)
        engine.Engine.start()
        Timber.tag(Constant.TAG).d("Started stun to socks")
    }

    @Synchronized
    override fun run() {
        try {
            Timber.tag(Constant.TAG).d("VPNService work thread is running... $ID")
            ProxyConfig.Instance.AppInstallID = BaseAppConfig.appInstallID
            Timber.tag(Constant.TAG).d("Android version: %s", Build.VERSION.RELEASE)
            waitUntilPreapred()
            runVPN()
        } catch (e: InterruptedException) {
            Timber.tag(Constant.TAG).d("Exception ${e.printStackTrace()}")
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.tag(Constant.TAG).d("Fatal error: %s", e.toString())
        }

        Timber.tag(Constant.TAG).d("VpnProxy terminated.")
    }

    @Throws(Exception::class)
    private fun runVPN() {
        m_VPNInterface = establishVPN()!!
//        val duplicatedFd  = Os.dup(m_VPNInterface!!.fileDescriptor)
//        vpnInterface.set(duplicatedFd)
        if (BaseAppConfig.proxy.isNotEmpty()) {
            startTunToSock(m_VPNInterface)
        }
//        protect(m_VPNInterface!!.detachFd())
//        this.m_VPNOutputStream = FileOutputStream(m_VPNInterface!!.getFileDescriptor())
//        val input = FileInputStream(m_VPNInterface!!.getFileDescriptor())
//        try {
//            while (IsRunning) {
//                var idle = true
//                val size = input.read(m_Packet)
//                if (size > 0) {
//                    m_VPNOutputStream!!.write(m_IPHeader.m_Data, m_IPHeader.m_Offset, size)
//                    idle = false
//                }
//                if (idle) {
//                    Thread.sleep(100)
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        } finally {
//            input.close()
//        }
    }

    private fun waitUntilPreapred() {
        while (prepare(this) != null) {
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                // Ignore
            }
        }
    }

    @Throws(Exception::class)
    private fun establishVPN(): ParcelFileDescriptor? {
        val builder: Builder = Builder()
        builder.setMtu(ProxyConfig.Instance.mTU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && BaseAppConfig.proxy.isNotEmpty()) {
            builder.setHttpProxy(ProxyInfo.buildDirectProxy("127.0.0.1", 8118))
        }

        builder.addAddress("10.0.0.2", 32)
        if (BaseAppConfig.proxy.isNotEmpty()) {
            builder.addRoute("0.0.0.0", 0)
        }

        if (BaseAppConfig.dnsServer.isNotEmpty()) {
            val dnsArray = BaseAppConfig.dnsServer.split(",")
            for (dns in dnsArray) {
                if (dns.isNotEmpty()) {
                    builder.addDnsServer(dns)
                }
            }
        }

        Timber.tag(Constant.TAG).d("VpnProxy add disallowed applications: %s", allowApp)
        if (allowApp?.isNotEmpty() == true) {
            for (app in allowApp!!) {
                builder.addDisallowedApplication(app)
            }
        }

        builder.addDisallowedApplication(packageName)

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        builder.setConfigureIntent(pendingIntent)

        builder.setSession(ProxyConfig.Instance.sessionName)
        val pfdDescriptor: ParcelFileDescriptor? = builder.establish()
        return pfdDescriptor
    }

//    override fun onTaskRemoved(rootIntent: Intent?) {
//        stopVPN()
//        super.onTaskRemoved(rootIntent)
//    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override fun onDestroy() {
        Timber.tag(Constant.TAG).d("VPNService($ID) destroyed")
        proxyConnection.updateUI(HomeFragment.DISCONNECTED)
        super.onDestroy()
    }

    companion object {
        private var allowApp: List<String>? = null
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"

        var Instance: LocalVpnService? = null
        var IsRunning: Boolean = false
        var version_bypass: String = "1.0.1"
        private var ID = 0
    }
}
