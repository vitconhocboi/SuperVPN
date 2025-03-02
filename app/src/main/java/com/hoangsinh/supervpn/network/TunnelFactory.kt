package com.hoangsinh.supervpn.network

import com.hoangsinh.supervpn.network.tunnel.RawTunnel
import com.hoangsinh.supervpn.network.tunnel.Tunnel
import com.hoangsinh.supervpn.network.tunnel.httpconnect.HttpConnectConfig
import com.hoangsinh.supervpn.network.tunnel.httpconnect.HttpConnectTunnel
import java.net.InetSocketAddress
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

object TunnelFactory {
    fun wrap(channel: SocketChannel?, selector: Selector?): Tunnel {
        return RawTunnel(channel, selector)
    }

    @Throws(Exception::class)
    fun createTunnelByConfig(destAddress: InetSocketAddress, selector: Selector?): Tunnel {
        if (destAddress.isUnresolved) {
            val config = ProxyConfig.Instance.getDefaultTunnelConfig(destAddress)
            if (config is HttpConnectConfig) {
                return HttpConnectTunnel(config, selector)
            }
            throw Exception("The config is unknow.")
        } else {
            return RawTunnel(destAddress, selector)
        }
    }
}
