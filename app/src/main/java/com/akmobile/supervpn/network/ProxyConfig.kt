package com.akmobile.supervpn.network

import android.annotation.SuppressLint
import android.content.Context
import com.common.baseui.BaseAppConfig
import com.akmobile.supervpn.network.tcpip.CommonMethods
import com.akmobile.supervpn.network.tunnel.Config
import com.akmobile.supervpn.network.tunnel.httpconnect.HttpConnectConfig
import com.akmobile.supervpn.utils.Constant
import java.net.InetSocketAddress
import java.util.Locale

class ProxyConfig {
    var m_IpList: ArrayList<IPAddress>
    var dnsList: ArrayList<IPAddress>
    var m_ProxyList: ArrayList<Config>
    var m_DomainMap: HashMap<String, Boolean>

    var dnsTTL: Int = 10
    var welcomeInfo: String = Constant.TAG
    var sessionName: String = Constant.TAG
    var userAgent: String = System.getProperty("http.agent")

    var mTU: Int = 1500
    var AppInstallID: String? = null


    init {
        m_IpList = ArrayList()
        dnsList = ArrayList()
        m_ProxyList = ArrayList<Config>()
        m_DomainMap = HashMap()

        //m_IpList.add(new IPAddress("26.26.26.2", 32));
        //m_IpList.add(new IPAddress("23.142.16.113", 32));
        //m_DnsList.add(new IPAddress("119.29.29.29"));
        //m_DnsList.add(new IPAddress("223.5.5.5"));
        dnsList.add(IPAddress("1.1.1.1"))
    }

    fun setPublicIp(IP: String?) {
        if (IP != null) {
            m_IpList.clear()
            m_IpList.add(IPAddress(IP, 32))
        }
    }

    val defaultProxy: Config
        get() {
            if (m_ProxyList.isEmpty()) {
                val config: Config = HttpConnectConfig.getProxyConfig()
                m_ProxyList.clear()
                m_ProxyList.add(config)
                return config
            } else {
                return m_ProxyList[0]
            }
        }

    fun getDefaultTunnelConfig(destAddress: InetSocketAddress?): Config {
        return defaultProxy
    }

    val defaultLocalIP: IPAddress
        get() {
            if (m_IpList.isEmpty()) {
                val publicIp = BaseAppConfig.publicIP
                val address: IPAddress = IPAddress(publicIp, 32)
                m_IpList.add(address)
                return address
            } else {
                return m_IpList[0]
            }
        }

    fun resetDomain(items: Array<String>) {
        m_DomainMap.clear()
        addDomainToHashMap(items, 0, false)
    }

    private fun addDomainToHashMap(items: Array<String>, offset: Int, state: Boolean) {
        var listBypass = ""
        for (i in offset until items.size) {
            var domainString = items[i].lowercase(Locale.getDefault()).trim { it <= ' ' }
            if (domainString.length == 0) continue
            if (domainString[0] == '.') {
                domainString = domainString.substring(1)
            }
            m_DomainMap[domainString] = state
            listBypass += """
                
                ${items[i]}
                """.trimIndent()
        }
        LocalVpnService.Instance.writeLog("Bypass: " + LocalVpnService.version_bypass + listBypass)
    }

    private fun getDomainState(domain: String): Boolean? {
        var domain = domain
        domain = domain.lowercase()
        while (domain.length > 0) {
            val stateBoolean = m_DomainMap[domain]
            if (stateBoolean != null) {
                return stateBoolean
            } else {
                val start = domain.indexOf('.') + 1
                if (start > 0 && start < domain.length) {
                    domain = domain.substring(start)
                } else {
                    return null
                }
            }
        }
        return null
    }

    fun needProxy(host: String?): Boolean {
        if (host != null) {
            val stateBoolean = getDomainState(host)!!
            if (stateBoolean != null) {
                return stateBoolean
            }
        }

        return true
    }

    fun needProxy(ip: Int): Boolean {
        if (ip > 0) {
            if (isFakeIP(ip)) {
                return true
            }
        }

        return true
    }


    inner class IPAddress {
        val Address: String
        val PrefixLength: Int

        constructor(address: String, prefixLength: Int) {
            this.Address = address
            this.PrefixLength = prefixLength
        }

        constructor(ipAddresString: String) {
            val arrStrings =
                ipAddresString.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val address = arrStrings[0]
            var prefixLength = 32
            if (arrStrings.size > 1) {
                prefixLength = arrStrings[1].toInt()
            }
            this.Address = address
            this.PrefixLength = prefixLength
        }

        override fun toString(): String {
            return String.format(Locale.ENGLISH, "%s/%d", Address, PrefixLength)
        }

        override fun equals(o: Any?): Boolean {
            return if (o == null) {
                false
            } else {
                this.toString() == o.toString()
            }
        }
    }

    companion object {
        val Instance: ProxyConfig = ProxyConfig()
        const val IS_DEBUG: Boolean = true //BuildConfig.DEBUG;
        val FAKE_NETWORK_MASK: Int = CommonMethods.ipStringToInt("255.255.0.0")
        val FAKE_NETWORK_IP: Int = CommonMethods.ipStringToInt("26.25.0.0")

        @SuppressLint("AuthLeak")
        fun getHttpProxyServer(ctx: Context): String? {
            return ctx.getSharedPreferences("proxyConfig", Context.MODE_PRIVATE)
                .getString("serverAddress", "http://qhipbvvb:1mwz1lgkcveg@154.95.36.199:6893")
        }

        fun isFakeIP(ip: Int): Boolean {
            return (ip and FAKE_NETWORK_MASK) == FAKE_NETWORK_IP
        }

        fun setHttpProxyServer(ctx: Context, address: String?) {
            ctx.getSharedPreferences("proxyConfig", Context.MODE_PRIVATE).edit()
                .putString("serverAddress", address).apply()
        }
    }
}
