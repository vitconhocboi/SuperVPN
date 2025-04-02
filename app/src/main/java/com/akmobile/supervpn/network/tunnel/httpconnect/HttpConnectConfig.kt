package com.akmobile.supervpn.network.tunnel.httpconnect

import android.net.Uri
import com.akmobile.supervpn.network.tunnel.Config
import com.common.baseui.BaseAppConfig
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
        fun getProxyConfig(): HttpConnectConfig {
            val config = HttpConnectConfig()
            config.UserName = BaseAppConfig.proxyUser
            config.Password = BaseAppConfig.proxyPass
            config.ServerAddress =
                InetSocketAddress(BaseAppConfig.proxyHost, BaseAppConfig.proxyPort.toInt())
            return config
        }
    }
}
