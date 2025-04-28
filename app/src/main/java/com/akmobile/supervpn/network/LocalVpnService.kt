package com.akmobile.supervpn.network

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ProxyInfo
import android.net.VpnService
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import com.akmobile.supervpn.home.HomeFragment
import com.akmobile.supervpn.main.MainActivity
import com.akmobile.supervpn.network.dns.DnsPacket
import com.akmobile.supervpn.network.tcpip.CommonMethods
import com.akmobile.supervpn.network.tcpip.IPHeader
import com.akmobile.supervpn.network.tcpip.TCPHeader
import com.akmobile.supervpn.network.tcpip.UDPHeader
import com.akmobile.supervpn.proxy.ProxyConnection
import com.akmobile.supervpn.utils.Constant
import com.common.baseui.BaseAppConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import timber.log.Timber
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class LocalVpnService : VpnService(), Runnable {

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    @Inject
    lateinit var proxyConnection: ProxyConnection

    private var m_VPNThread: Thread? = null
    private var m_VPNInterface: ParcelFileDescriptor? = null
    private var m_VPNOutputStream: FileOutputStream? = null

    private val m_Packet: ByteArray
    private val m_IPHeader: IPHeader

    private var m_PrivoxyManager: PrivoxyManager? = null
    private var allowApp: List<String>? = null

    init {
        ID++
        m_Packet = ByteArray(20000)
        m_IPHeader = IPHeader(m_Packet, 0)
        Instance = this
    }

    override fun onCreate() {
        try {
            Timber.tag(Constant.TAG).d("LocalTcpServer started.")
        } catch (e: Exception) {
            Timber.tag(Constant.TAG).d("Failed to start TCP/DNS Proxy")
        }

        super.onCreate()
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
                // Start a new session by creating a new thread.
                m_VPNThread = Thread(this, "VPNServiceThread")
                m_VPNThread!!.start()

                m_PrivoxyManager = PrivoxyManager(this)
                if (m_PrivoxyManager!!.initialize() && m_PrivoxyManager!!.start()) {
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

                    // Close streams first
                    m_VPNOutputStream?.close()
                    m_VPNOutputStream = null

                    // Detach file descriptor before stopping engine
                    if (m_VPNInterface != null) {
                        try {
                            val fd = m_VPNInterface!!.detachFd()
                            Timber.tag(Constant.TAG).d("Successfully detached fd: $fd")
                        } catch (e: Exception) {
                            Timber.tag(Constant.TAG)
                                .d("Error detaching VPN interface fd ${e.printStackTrace()}")
                        }
                        m_VPNInterface = null
                    }

                    // Now stop engine after fd is detached
                    engine.Engine.stop()

                    // Stop other components
                    m_PrivoxyManager?.stop()
                    m_PrivoxyManager = null

                    stopSelf() // Stop the service after cleanup
                }
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return LocalBinder(this)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        // Ensure clean unbinding
        try {
            if (IsRunning) {
                // If still running, initiate cleanup
                stopProxy(this)
            }
        } catch (e: Exception) {
            Timber.tag(Constant.TAG).e("Error during service unbind ${e.printStackTrace()}")
        }
        return false
    }

    class LocalBinder(val service: LocalVpnService) : Binder()

    override fun onRevoke() {
        Timber.tag(Constant.TAG).e("VPN has been revoked (likely by another VPN)")
        stopSelf()
    }

    private fun startTunToSock(pfdDescriptor: ParcelFileDescriptor? = null) {
        val key = engine.Key()
        key.mark = 0
        key.mtu = ProxyConfig.Instance.mTU.toLong()
        key.device = "fd://" + pfdDescriptor?.fd
        key.logLevel = "debug"
        val proxyType = BaseAppConfig.proxyType
        if (proxyType.lowercase() == "socks5") {
            val proxyHost = BaseAppConfig.proxyHost
            val proxyPort = BaseAppConfig.proxyPort
            val proxyUser = BaseAppConfig.proxyUser
            val proxyPass = BaseAppConfig.proxyPass
            key.proxy = "socks5://$proxyUser:$proxyPass@$proxyHost:$proxyPort"
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
        this.m_VPNInterface = establishVPN()!!
        startTunToSock(m_VPNInterface)
        protect(m_VPNInterface!!.detachFd())
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setHttpProxy(ProxyInfo.buildDirectProxy("127.0.0.1", 8118))
        }

        builder.addAddress("10.0.0.2", 32)
        if (BaseAppConfig.proxyType.lowercase() == "socks5") {
            builder.addRoute("0.0.0.0", 1)
        } else {
            builder.addRoute("128.0.0.0", 1)
        }

        for (dns in ProxyConfig.Instance.dnsList) {
            builder.addDnsServer(dns.Address)
        }

//        if (allowApp?.isNotEmpty() == true) {
//            for (app in allowApp!!) {
//                builder.addAllowedApplication(app)
//            }
//        }

//        builder.addDisallowedApplication(packageName)

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
        proxyConnection.updateUI(HomeFragment.DISCONNECTED)
        super.onDestroy()
    }

    companion object {

        private const val ACTION_START = "ACTION_START"
        private const val ACTION_STOP = "ACTION_STOP"

        lateinit var Instance: LocalVpnService
        var IsRunning: Boolean = false
        var version_bypass: String = "1.0.1"
        private var ID = 0

        fun startProxy(context: Context, allowApp: List<String>?) {
            context.startService(
                Intent(context, LocalVpnService::class.java).apply {
                    action = ACTION_START
                    putStringArrayListExtra("allowApp", allowApp as ArrayList<String>?)
                }
            )
        }

        fun stopProxy(context: Context) {
            context.startService(
                Intent(context, LocalVpnService::class.java).apply {
                    action = ACTION_STOP
                }
            )
        }
    }
}
