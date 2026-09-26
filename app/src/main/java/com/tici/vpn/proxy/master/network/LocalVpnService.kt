package com.tici.vpn.proxy.master.network

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Binder
import android.os.Build
import android.os.DeadObjectException
import android.os.IBinder
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.common.baseui.BaseAppConfig
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.home.HomeFragment
import com.tici.vpn.proxy.master.main.MainActivity
import com.tici.vpn.proxy.master.settings.adsblock.AdRulesCrashGuard
import com.tici.vpn.proxy.master.settings.adsblock.AdsBlockInterface
import com.tici.vpn.proxy.master.utils.Constant
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.io.FileDescriptor
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

@AndroidEntryPoint
class LocalVpnService : VpnService(), Runnable {
    @Volatile
    private var m_VPNThread: Thread? = null
    private var m_VPNInterface: ParcelFileDescriptor? = null

    @Inject
    lateinit var adsBlockRepository: AdsBlockInterface

    @Inject
    lateinit var crashGuard: AdRulesCrashGuard

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val isFilterRunning: Boolean get() = IsRunning

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    @Inject
    lateinit var proxyConnection: VpnStateListener
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
                } else {
                    startForeground(
                        1,
                        buildNotification()
                    )
                }

                m_VPNThread = Thread(this, "VPNServiceThread")
                m_VPNThread!!.start()
            }

            ACTION_STOP -> {
                stopVPN()
                stopForeground(true)
            }
        }

        return START_NOT_STICKY
    }

    /**
     * Renders the blocklist file from Room (honours the ad-block toggle and crash quarantine).
     * Runs on the VPN thread before the engine starts, so start never races a stale file.
     * @return the file path, or "" to start without blocking.
     */
    private fun writeBlocklist(): String = try {
        val body = runBlocking { adsBlockRepository.renderBlocklist() }
        EngineBlocklistFile.write(this, body).absolutePath
    } catch (e: Exception) {
        Timber.tag(Constant.TAG).e(e, "Failed to prepare blocklist")
        ""
    }

    /** Persisted MITM CA for path rules; null disables MITM (path rules then pass through). */
    private fun loadMitmCa(): MitmCaStore.Pem? = try {
        MitmCaStore.loadOrCreate(this)
    } catch (e: Exception) {
        Timber.tag(Constant.TAG).e(e, "MITM CA unavailable; path rules disabled")
        null
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun stopVPN() {
        if (IsRunning) {
            IsRunning = false
            if (m_VPNThread != null) {
                m_VPNThread!!.interrupt()
                try {
                    m_VPNThread!!.join(1000)
                } catch (e: InterruptedException) {
                    Timber.tag(Constant.TAG).d("VPN thread interrupt error")
                }
                m_VPNThread = null
            }

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

            engine.Engine.stop()

            if (Instance != null) {
                Instance!!.stopForeground(true)
                Instance!!.stopSelf()
                Instance = null
            }
            proxyConnection.updateUI(HomeFragment.DISCONNECTED)
            Timber.tag(Constant.TAG).d("VPNService stopped.")
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return LocalBinder(this)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        try {
            if (IsRunning) {
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

    private fun startTunToSock(
        pfdDescriptor: ParcelFileDescriptor,
        blocklistPath: String,
        ca: MitmCaStore.Pem?
    ) {
        val key = engine.Key()
        key.setMTU(ProxyConfig.Instance.mTU.toLong())
        key.setFD(pfdDescriptor.fd)
        key.setLogLevel("silent")
        key.setDNSServers(BaseAppConfig.dnsServer)
        key.setBlocklistPath(blocklistPath)
        key.setCACertPEM(ca?.certPem.orEmpty())
        key.setCAKeyPEM(ca?.keyPem.orEmpty())
        engine.Engine.insert(key)
        engine.Engine.start()
        proxyConnection.updateUI(HomeFragment.CONNECTED)
        Timber.tag(Constant.TAG).d("Started netstack engine")
    }

    @Synchronized
    override fun run() {
        try {
            Timber.tag(Constant.TAG).d("VPNService work thread is running... $ID")
            ProxyConfig.Instance.AppInstallID = BaseAppConfig.appInstallID
            Timber.tag(Constant.TAG).d("Android version: %s", Build.VERSION.RELEASE)
            waitUntilPrepared()
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
        // Slow prep first (Room query, Keystore, first-run CA keygen) so a stop landing during it
        // never leaves a tun fd or a running engine behind.
        val blocklistPath = writeBlocklist()
        val ca = loadMitmCa()
        if (!isCurrentVpnThread()) return
        val pfd = establishVPN()!!
        m_VPNInterface = pfd
        startTunToSock(pfd, blocklistPath, ca)
        if (!isCurrentVpnThread()) {
            // stopVPN() ran mid-start and could not see this engine; without this the next
            // connect fails with "engine: already started".
            engine.Engine.stop()
            pfd.close()
            proxyConnection.updateUI(HomeFragment.DISCONNECTED)
        }
    }

    /** stopVPN() (or a newer start) replaces/clears [m_VPNThread]; a stale thread must bail out. */
    private fun isCurrentVpnThread() = m_VPNThread === Thread.currentThread()

    private fun waitUntilPrepared() {
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

        builder.addAddress("10.0.0.2", 32)
        builder.addRoute("0.0.0.0", 0)

        if (BaseAppConfig.dnsServer.isNotEmpty()) {
            val dnsArray = BaseAppConfig.dnsServer.split(",")
            for (dns in dnsArray) {
                if (dns.isNotEmpty()) {
                    builder.addDnsServer(dns.trim())
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

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override fun onDestroy() {
        Timber.tag(Constant.TAG).d("VPNService($ID) destroyed")
        serviceScope.cancel()
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
