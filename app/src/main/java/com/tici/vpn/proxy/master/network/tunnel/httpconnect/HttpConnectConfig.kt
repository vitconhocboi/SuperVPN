package com.tici.vpn.proxy.master.network.tunnel.httpconnect

import com.tici.vpn.proxy.master.network.tunnel.Config

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
}
