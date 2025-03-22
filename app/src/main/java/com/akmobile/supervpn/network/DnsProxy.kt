package com.akmobile.supervpn.network

import android.util.Log
import android.util.SparseArray
import com.akmobile.supervpn.network.dns.DnsPacket
import com.akmobile.supervpn.network.dns.ResourcePointer
import com.akmobile.supervpn.network.tcpip.CommonMethods
import com.akmobile.supervpn.network.tcpip.IPHeader
import com.akmobile.supervpn.network.tcpip.UDPHeader
import com.akmobile.supervpn.utils.Constant
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

class DnsProxy : Runnable {
    private val QUERY_TIMEOUT_NS = 10 * 1000000000L
    var Stopped = false
    private var m_Client: DatagramSocket? = null
    private var m_ReceivedThread: Thread? = null
    private var m_QueryID: Short = 0
    private val m_QueryArray: SparseArray<QueryState> = SparseArray()

    init {
        m_Client = DatagramSocket(0)
    }

    @Synchronized
    fun start() {
        m_ReceivedThread = Thread(this)
        m_ReceivedThread?.name = "DnsProxyThread"
        m_ReceivedThread?.start()
    }

    @Synchronized
    fun stop() {
        Stopped = true
        m_Client?.let {
            try {
                it.close()
            } catch (e: Exception) {
                Log.e(Constant.TAG, "Exception when closing m_Client", e)
            } finally {
                m_Client = null
            }
        }
    }

    override fun run() {
        try {
            val RECEIVE_BUFFER = ByteArray(2000)
            val ipHeader = IPHeader(RECEIVE_BUFFER, 0)
            ipHeader.Default()
            val udpHeader = UDPHeader(RECEIVE_BUFFER, 20)

            var dnsBuffer = ByteBuffer.wrap(RECEIVE_BUFFER)
            dnsBuffer.position(28)
            dnsBuffer = dnsBuffer.slice()

            val packet = DatagramPacket(RECEIVE_BUFFER, 28, RECEIVE_BUFFER.size - 28)

            while (m_Client != null && !m_Client!!.isClosed) {
                packet.length = RECEIVE_BUFFER.size - 28
                m_Client!!.receive(packet)

                dnsBuffer.clear()
                dnsBuffer.limit(packet.length)
                try {
                    val dnsPacket = DnsPacket.FromBytes(dnsBuffer)
                    dnsPacket?.let {
                        OnDnsResponseReceived(ipHeader, udpHeader, it)
                    }
                } catch (e: Exception) {
                    Log.e(Constant.TAG, "Exception when reading DNS packet", e)
                }
            }
        } catch (e: Exception) {
            Log.e(Constant.TAG, "Exception in DnsResolver main loop", e)
        } finally {
            Log.d(Constant.TAG, "DnsResolver Thread Exited.")
            this.stop()
        }
    }

//    private fun getFirstIP(dnsPacket: DnsPacket): Int {
//        for (i in 0 until dnsPacket.Header?.resourceCount!!) {
//            val resource = dnsPacket.Resources?.get(i)
//            if (resource?.Type?.toInt() == 1) {
//                return CommonMethods.readInt(resource.Data, 0)
//            }
//        }
//        return 0
//    }

    private fun tamperDnsResponse(rawPacket: ByteArray, dnsPacket: DnsPacket, newIP: Int) {
        val question = dnsPacket.Questions!!.get(0)
        question!!.let {
            dnsPacket.Header?.resourceCount = (1.toShort())
            dnsPacket.Header?.aResourceCount = (0.toShort())
            dnsPacket.Header?.eResourceCount = (0.toShort())

            val rPointer = ResourcePointer(rawPacket, question.Offset() + question.Length())
            rPointer.setDomain(0xC00C.toShort())
            rPointer.type = question.Type
            rPointer.setClass(question.Class)
            rPointer.tTL = ProxyConfig.Instance.dnsTTL
            rPointer.dataLength = 4.toShort()
            rPointer.iP = newIP

            dnsPacket.Size = 12 + question.Length() + 16
        }

    }

    private fun getOrCreateFakeIP(domainString: String): Int {
        DomainIPMaps[domainString]?.let { return it }

        var hashIP = domainString.hashCode()
        var fakeIP: Int
        do {
            fakeIP = ProxyConfig.FAKE_NETWORK_IP or (hashIP and 0x0000FFFF)
            hashIP++
        } while (IPDomainMaps.containsKey(fakeIP))

        DomainIPMaps[domainString] = fakeIP
        IPDomainMaps[fakeIP] = domainString
        return fakeIP
    }

    private fun OnDnsResponseReceived(
        ipHeader: IPHeader,
        udpHeader: UDPHeader,
        dnsPacket: DnsPacket
    ) {
        var state: QueryState? = null
        synchronized(m_QueryArray) {
            state = m_QueryArray[dnsPacket.Header?.id!!.toInt()]
            state?.let {
                m_QueryArray.remove(dnsPacket.Header?.id!!.toInt())
            }
        }

        state?.let {
            dnsPacket.Header?.id = (it.ClientQueryID)
            ipHeader.sourceIP = it.RemoteIP
            ipHeader.destinationIP = it.ClientIP
            ipHeader.protocol = IPHeader.UDP
            ipHeader.totalLength = 20 + 8 + dnsPacket.Size
            udpHeader.sourcePort = it.RemotePort
            udpHeader.destinationPort = it.ClientPort
            udpHeader.totalLength = 8 + dnsPacket.Size

            LocalVpnService.Instance.sendUDPPacket(ipHeader, udpHeader)
        }
    }

    private fun getIPFromCache(domain: String): Int {
        return DomainIPMaps[domain] ?: 0
    }

    private fun interceptDns(
        ipHeader: IPHeader,
        udpHeader: UDPHeader,
        dnsPacket: DnsPacket
    ): Boolean {
        val question = dnsPacket.Questions?.get(0)

        if (ProxyConfig.IS_DEBUG) Log.d(Constant.TAG, "DNS Query ${question?.Domain}")

        if (question?.Type?.toInt() == 1) {
            val need = ProxyConfig.Instance.needProxy(question.Domain)
            Log.d(Constant.TAG, "LONGLD DNS Query ${question.Domain} ---> $need")
            if (need) {
                val fakeIP = question?.Domain?.let { getOrCreateFakeIP(it) }
                if (fakeIP != null) {
                    tamperDnsResponse(ipHeader.m_Data, dnsPacket, fakeIP)
                }

                if (ProxyConfig.IS_DEBUG) {
                    Log.d(
                        Constant.TAG,
                        "LONGLD interceptDns FakeDns: ${question.Domain} ${
                            fakeIP?.let {
                                CommonMethods.ipIntToString(
                                    it
                                )
                            }
                        }"
                    )
                }

                val sourceIP = ipHeader.sourceIP
                val sourcePort = udpHeader.sourcePort
                ipHeader.sourceIP = ipHeader.destinationIP
                ipHeader.destinationIP = sourceIP
                ipHeader.totalLength = 20 + 8 + dnsPacket.Size
                udpHeader.sourcePort = udpHeader.destinationPort
                udpHeader.destinationPort = sourcePort
                udpHeader.totalLength = 8 + dnsPacket.Size
                LocalVpnService.Instance.sendUDPPacket(ipHeader, udpHeader)
                return true
            }
        }
        return false
    }

    private fun clearExpiredQueries() {
        val now = System.nanoTime()
        for (i in m_QueryArray.size() - 1 downTo 0) {
            val state = m_QueryArray.valueAt(i)
            if ((now - state.QueryNanoTime) > QUERY_TIMEOUT_NS) {
                m_QueryArray.removeAt(i)
            }
        }
    }

    fun onDnsRequestReceived(ipHeader: IPHeader, udpHeader: UDPHeader, dnsPacket: DnsPacket) {
        if (!interceptDns(ipHeader, udpHeader, dnsPacket)) {
            val state = QueryState()
            state.ClientQueryID = dnsPacket?.Header?.id!!
            state.QueryNanoTime = System.nanoTime()
            state.ClientIP = ipHeader.sourceIP
            state.ClientPort = udpHeader.sourcePort
            state.RemoteIP = ipHeader.destinationIP
            state.RemotePort = udpHeader.destinationPort

            m_QueryID++
            dnsPacket.Header?.id = (m_QueryID)

            synchronized(m_QueryArray) {
                clearExpiredQueries()
                m_QueryArray.put(m_QueryID.toInt(), state)
            }
            Log.d("LONGLD", "LONGLD onDnsRequestReceived: $state")
            val remoteAddress = InetSocketAddress(
                CommonMethods.ipIntToInet4Address(state.RemoteIP),
                state.RemotePort.toInt()
            )
            val packet = DatagramPacket(udpHeader.m_Data, udpHeader.m_Offset + 8, dnsPacket.Size)
            packet.socketAddress = remoteAddress

            try {
                if (LocalVpnService.Instance.protect(m_Client)) {
                    m_Client?.send(packet)
                } else {
                    Log.e(Constant.TAG, "VPN protect udp socket failed.")
                }
            } catch (e: IOException) {
                Log.e(Constant.TAG, "protect", e)
            }
        }
    }

    private inner class QueryState {
        var ClientQueryID: Short = 0
        var QueryNanoTime: Long = 0
        var ClientIP: Int = 0
        var ClientPort: Short = 0
        var RemoteIP: Int = 0
        var RemotePort: Short = 0
    }

    companion object {
        private val IPDomainMaps = ConcurrentHashMap<Int, String>()
        private val DomainIPMaps = ConcurrentHashMap<String, Int>()
        fun reverseLookup(ip: Int): String? {
            return IPDomainMaps[ip]
        }
    }
}
