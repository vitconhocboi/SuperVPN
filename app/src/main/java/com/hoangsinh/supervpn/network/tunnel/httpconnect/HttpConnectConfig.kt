package com.hoangsinh.supervpn.network.tunnel.httpconnect

import android.net.Uri
import com.hoangsinh.supervpn.network.tunnel.Config
import java.net.InetSocketAddress

class HttpConnectConfig : Config() {
    var UserName: String? = null
    var Password: String? = null

    override fun equals(o: Any?): Boolean {
        if (o == null) return false
        return this.toString() == o.toString()
    }

    override fun toString(): String {
        return java.lang.String.format("http://%s:%s@%s", UserName, Password, ServerAddress)
    }

    companion object {
        fun parse(proxyInfo: String?): HttpConnectConfig {
            val config = HttpConnectConfig()
            val uri = Uri.parse(proxyInfo)
            val userInfoString = uri.userInfo
            if (userInfoString != null) {
                val userStrings = userInfoString.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                config.UserName = userStrings[0]
                if (userStrings.size >= 2) {
                    config.Password = userStrings[1]
                }
            }
            config.ServerAddress = InetSocketAddress(uri.host, uri.port)
            return config
        }
    }
}
