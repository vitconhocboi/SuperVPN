package com.hoangsinh.supervpn.network.tunnel

import java.net.InetSocketAddress

abstract class Config {
    open var ServerAddress: InetSocketAddress? = null
}
