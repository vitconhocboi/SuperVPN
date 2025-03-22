package com.akmobile.supervpn.network

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ProxyInfo
import android.net.VpnService
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.util.Log
import com.common.baseui.BaseAppConfig
import com.akmobile.supervpn.MainActivity
import com.akmobile.supervpn.network.dns.DnsPacket
import com.akmobile.supervpn.network.tcpip.CommonMethods
import com.akmobile.supervpn.network.tcpip.IPHeader
import com.akmobile.supervpn.network.tcpip.TCPHeader
import com.akmobile.supervpn.network.tcpip.UDPHeader
import com.akmobile.supervpn.utils.Constant
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

//import go.lantern.Lantern;
class LocalVpnService : VpnService(), Runnable {
    private val device: String = Build.DEVICE
    private val model: String = Build.MODEL
    private val version = "" + Build.VERSION.SDK_INT + " (" + Build.VERSION.RELEASE + ")"

    private var m_VPNThread: Thread? = null
    private var m_VPNInterface: ParcelFileDescriptor? = null
    private var m_TcpProxyServer: TcpProxyServer? = null
    private var m_DnsProxy: DnsProxy? = null
    private var m_VPNOutputStream: FileOutputStream? = null

    private val m_Packet: ByteArray
    private val m_IPHeader: IPHeader
    private val m_TCPHeader: TCPHeader
    private val m_UDPHeader: UDPHeader
    private val m_DNSBuffer: ByteBuffer
    private val m_Handler: Handler
    private var m_SentBytes: Long = 0
    private var m_ReceivedBytes: Long = 0
    private lateinit var m_Blacklist: Array<String>

    init {
        ID++
        m_Handler = Handler()
        m_Packet = ByteArray(20000)
        m_IPHeader = IPHeader(m_Packet, 0)
        m_TCPHeader = TCPHeader(m_Packet, 20)
        m_UDPHeader = UDPHeader(m_Packet, 20)
        m_DNSBuffer = (ByteBuffer.wrap(m_Packet).position(28) as ByteBuffer).slice()
        Instance = this

        Log.d("VpnProxy", "New VPNService" + ID)
    }

    override fun onCreate() {/*
        writeLog("This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.");
        writeLog("This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.");

        writeLog("This program includes two other open source programs:");
        writeLog("SmartProxy Copyright (C) 2014 hedaode. GPLv3");
        writeLog("Lantern Copyright 2010 Brave New Software Project, Inc. Apache 2.0");
*/

        try {
            m_TcpProxyServer = TcpProxyServer(0)
            m_TcpProxyServer!!.start()
            writeLog("LocalTcpServer started.")

            m_DnsProxy = DnsProxy()
            m_DnsProxy!!.start()
            writeLog("LocalDnsProxy started.")
        } catch (e: Exception) {
            writeLog("Failed to start TCP/DNS Proxy")
        }

        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        IsRunning = true
        // Start a new session by creating a new thread.
        m_VPNThread = Thread(this, "VPNServiceThread")
        m_VPNThread!!.start()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        val action = intent.action
        if (action == SERVICE_INTERFACE) {
            return super.onBind(intent)
        }
        return null
    }

    private fun onStatusChanged(status: String, isRunning: Boolean) {
        m_Handler.post {
            for ((key) in m_OnStatusChangedListeners) {
                key.onStatusChanged(status, isRunning)
            }
        }
    }

    fun writeLog(format: String?, vararg args: Any?) {
        val logString = String.format(format!!, *args)
        m_Handler.post {
            for ((key) in m_OnStatusChangedListeners) {
                key.onLogReceived(logString)
            }
        }
    }

    fun sendUDPPacket(ipHeader: IPHeader, udpHeader: UDPHeader?) {
        try {
            CommonMethods.ComputeUDPChecksum(ipHeader, udpHeader!!)
            m_VPNOutputStream!!.write(ipHeader.m_Data, ipHeader.m_Offset, ipHeader.totalLength)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun SetProxyServer(addr: String) {
        Log.d("LongLD", "SetProxyServer: $addr")
        val preferences: SharedPreferences = getSharedPreferences(Constant.TAG, MODE_PRIVATE)
        val editor: SharedPreferences.Editor = preferences.edit()
        editor.putString("ProxyAddress", addr)
        editor.commit()
    }


    fun configDirFor(context: Context, suffix: String): String {
        return File(context.filesDir.absolutePath, ".lantern$suffix").absolutePath
    }

    @Synchronized
    override fun run() {
        try {
            Log.d(Constant.TAG, "VPNService work thread is running... $ID")
            ProxyConfig.Instance.AppInstallID = BaseAppConfig.appInstallID
            writeLog("Android version: %s", Build.VERSION.RELEASE)

            waitUntilPreapred()
            runVPN()
        } catch (e: InterruptedException) {
            Log.e(Constant.TAG, "Exception", e)
        } catch (e: Exception) {
            e.printStackTrace()
            writeLog("Fatal error: %s", e.toString())
        }

        writeLog("VpnProxy terminated.")
        dispose()
    }

    @Throws(Exception::class)
    private fun runVPN() {
        this.m_VPNInterface = establishVPN()!!
        this.m_VPNOutputStream = FileOutputStream(m_VPNInterface!!.getFileDescriptor())
        val input = FileInputStream(m_VPNInterface!!.getFileDescriptor())
        try {
            while (IsRunning) {
                var idle = true
                val size = input.read(m_Packet)
                if (size > 0) {
                    if (m_DnsProxy!!.Stopped || m_TcpProxyServer!!.Stopped) {
                        input.close()
                        throw Exception("LocalServer stopped.")
                    }
                    try {
                        onIPPacketReceived(m_IPHeader, size)
                        idle = false
                    } catch (ex: IOException) {
                        Log.e(Constant.TAG, "IOException when processing IP packet", ex)
                    }
                }
                if (idle) {
                    Thread.sleep(100)
                }
            }
        } finally {
            input.close()
        }
    }

    @Throws(IOException::class)
    fun onIPPacketReceived(ipHeader: IPHeader, size: Int) {
        when (ipHeader.protocol) {
            IPHeader.TCP -> {
                val tcpHeader: TCPHeader = m_TCPHeader
                tcpHeader.m_Offset = ipHeader.headerLength
                if (ipHeader.sourceIP === LOCAL_IP) {
                    if (tcpHeader.sourcePort === m_TcpProxyServer?.Port) {
                        val session =
                            NatSessionManager.getSession(tcpHeader.destinationPort.toInt())
                        if (session != null) {
                            ipHeader.sourceIP = ipHeader.destinationIP
                            tcpHeader.sourcePort = session.RemotePort
                            ipHeader.destinationIP = LOCAL_IP

                            CommonMethods.ComputeTCPChecksum(ipHeader, tcpHeader)
                            m_VPNOutputStream!!.write(ipHeader.m_Data, ipHeader.m_Offset, size)
                            m_ReceivedBytes += size.toLong()
                        } else {
                            if (ProxyConfig.IS_DEBUG) Log.d(
                                Constant.TAG,
                                ("NoSession: " + ipHeader.toString()).toString() + " " + tcpHeader.toString()
                            )
                        }
                    } else {
                        val portKey = tcpHeader.sourcePort
                        var session = NatSessionManager.getSession(portKey.toInt())
                        if (session == null || session.RemoteIP != ipHeader.destinationIP || session.RemotePort != tcpHeader.destinationPort) {
                            session = NatSessionManager.createSession(
                                portKey.toInt(), ipHeader.destinationIP, tcpHeader.destinationPort
                            )
                        }

                        session.LastNanoTime = System.nanoTime()
                        session.PacketSent++

                        val tcpDataSize = ipHeader.dataLength - tcpHeader.headerLength
                        if (session.PacketSent == 2 && tcpDataSize == 0) {
                            return
                        }

                        if (session.BytesSent == 0 && tcpDataSize > 10) {
                            val dataOffset = tcpHeader.m_Offset + tcpHeader.headerLength
                            val host = HttpHostHeaderParser.parseHost(
                                tcpHeader.m_Data, dataOffset, tcpDataSize
                            )
                            if (host != null) {
                                session.RemoteHost = host
                            }
                        }

                        ipHeader.sourceIP = ipHeader.destinationIP
                        ipHeader.destinationIP = LOCAL_IP
                        tcpHeader.destinationPort = m_TcpProxyServer!!.Port

                        CommonMethods.ComputeTCPChecksum(ipHeader, tcpHeader)
                        m_VPNOutputStream!!.write(ipHeader.m_Data, ipHeader.m_Offset, size)
                        session.BytesSent += tcpDataSize
                        m_SentBytes += size.toLong()
                    }
                }
            }

            IPHeader.UDP -> {
                val udpHeader: UDPHeader = m_UDPHeader
                udpHeader.m_Offset = ipHeader.headerLength
                if (ipHeader.sourceIP === LOCAL_IP && udpHeader.destinationPort === 53.toShort()) {
                    m_DNSBuffer.clear()
                    m_DNSBuffer.limit(ipHeader.dataLength - 8)
                    val dnsPacket = DnsPacket.FromBytes(m_DNSBuffer)
                    if (dnsPacket != null && dnsPacket.Header!!.questionCount > 0) {
                        m_DnsProxy!!.onDnsRequestReceived(ipHeader, udpHeader, dnsPacket)
                    }
                }
            }
        }
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
        NatSessionManager.clearAllSessions()

        val builder: Builder = Builder()
        builder.setMtu(ProxyConfig.Instance.mTU)
        //longld set current proxy to reference
        val proxyAddress: String = BaseAppConfig.proxyAddress
        if (proxyAddress != null) {
            this.SetProxyServer(proxyAddress)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setHttpProxy(ProxyInfo.buildDirectProxy("192.168.1.102", 8080))
        }

        val ipAddress = ProxyConfig.Instance.defaultLocalIP
        LOCAL_IP = CommonMethods.ipStringToInt(ipAddress.Address)

        builder.addAddress(ipAddress.Address, ipAddress.PrefixLength)
        if (ProxyConfig.IS_DEBUG) Log.d(
            Constant.TAG, java.lang.String.format(
                "addAddress: %s/%d\n", ipAddress.Address, ipAddress.PrefixLength
            )
        )

        for (dns in ProxyConfig.Instance.dnsList) {
            builder.addDnsServer(dns.Address)
        }

        m_Blacklist = byPassURL
        ProxyConfig.Instance.resetDomain(m_Blacklist)

//        for (routeAddress in getResources().getStringArray(R.array.bypass_private_route)) {
//            val addr =
//                routeAddress.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//            builder.addRoute(addr[0], addr[1].toInt())
//        }

        builder.addRoute(CommonMethods.ipIntToString(ProxyConfig.FAKE_NETWORK_IP), 16)

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            val packageManager = getPackageManager()
//            val list: List<PackageInfo> = packageManager.getInstalledPackages(0)
//            val packageSet = HashSet<String>()
//
//            for (i in list.indices) {
//                val info = list[i]
//                packageSet.add(info.packageName)
//            }
//
//            for (name in getResources().getStringArray(R.array.bypass_package_name)) {
//                if (packageSet.contains(name)) {
//                    builder.addDisallowedApplication(name)
//                }
//            }
//        }

        val intent: Intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        builder.setConfigureIntent(pendingIntent)

        builder.setSession(ProxyConfig.Instance.sessionName)
        val pfdDescriptor: ParcelFileDescriptor? = builder.establish()
        onStatusChanged(
            ProxyConfig.Instance.sessionName + " connected", true
        )
        return pfdDescriptor
    }

    @Synchronized
    private fun dispose() {
        onStatusChanged(
            ProxyConfig.Instance.sessionName + " disconnected", false
        )

        IsRunning = false

        try {
            if (m_VPNInterface != null) {
                m_VPNInterface!!.close()
                m_VPNInterface = null
            }
        } catch (e: Exception) {
            // ignore
        }

        try {
            if (m_VPNOutputStream != null) {
                m_VPNOutputStream!!.close()
                m_VPNOutputStream = null
            }
        } catch (e: Exception) {
            // ignore
        }

        //        try {
//            Lantern.RemoveOverrides();
//        } catch (Exception e) {
//            // Ignore
//        }
        if (m_VPNThread != null) {
            m_VPNThread!!.interrupt()
            m_VPNThread = null
        }
    }

    override fun onDestroy() {
        Log.d(Constant.TAG, "VPNService(%s) destroyed: " + ID)
        if (IsRunning) dispose()
        try {
            // ֹͣTcpServer
            if (m_TcpProxyServer != null) {
                m_TcpProxyServer!!.stop()
                m_TcpProxyServer = null
                // writeLog("LocalTcpServer stopped.");
            }
        } catch (e: Exception) {
            // ignore
        }
        try {
            // DnsProxy
            if (m_DnsProxy != null) {
                m_DnsProxy!!.stop()
                m_DnsProxy = null
                // writeLog("LocalDnsProxy stopped.");
            }
        } catch (e: Exception) {
            // ignore
        }
        super.onDestroy()
    }

    interface onStatusChangedListener {
        fun onStatusChanged(status: String?, isRunning: Boolean?)

        fun onLogReceived(logString: String?)
    }

    protected val byPassURL: Array<String>
        get() {
            var byPassLink = emptyArray<String>()
            return byPassLink
        }

    companion object {
        lateinit var Instance: LocalVpnService
        var IsRunning: Boolean = false
        var version_bypass: String = "1.0.1"
        private var ID = 0
        private var LOCAL_IP = 0
        private val m_OnStatusChangedListeners = ConcurrentHashMap<onStatusChangedListener, Any?>()

        fun addOnStatusChangedListener(listener: onStatusChangedListener) {
            if (!m_OnStatusChangedListeners.containsKey(listener)) {
                m_OnStatusChangedListeners[listener] = 1
            }
        }

        fun removeOnStatusChangedListener(listener: onStatusChangedListener) {
            if (m_OnStatusChangedListeners.containsKey(listener)) {
                m_OnStatusChangedListeners.remove(listener)
            }
        }
    }
}
