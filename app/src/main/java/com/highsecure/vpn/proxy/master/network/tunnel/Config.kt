package com.highsecure.vpn.proxy.master.network.tunnel

import java.net.InetSocketAddress

abstract class Config {
    open var ServerAddress: InetSocketAddress? = null
}
