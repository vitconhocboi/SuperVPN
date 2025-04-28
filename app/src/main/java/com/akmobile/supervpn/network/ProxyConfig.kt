package com.akmobile.supervpn.network

import com.akmobile.supervpn.network.tcpip.CommonMethods
import com.akmobile.supervpn.utils.Constant
import java.util.Locale

class ProxyConfig {
    var dnsList: ArrayList<IPAddress>

    var sessionName: String = Constant.TAG
    var userAgent: String = System.getProperty("http.agent")

    var mTU: Int = 1500
    var AppInstallID: String? = null


    init {
        dnsList = ArrayList()
        dnsList.add(IPAddress("8.8.8.8"))
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
        val FAKE_NETWORK_IP: Int = CommonMethods.ipStringToInt("26.25.0.0")
    }
}
