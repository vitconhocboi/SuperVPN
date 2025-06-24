package com.tici.vpn.proxy.master.network.tunnel

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

class RawTunnel : Tunnel {
    constructor(serverAddress: InetSocketAddress?, selector: Selector?) : super(
        serverAddress, selector
    )

    constructor(innerChannel: SocketChannel?, selector: Selector?) : super(innerChannel, selector)

    override fun onConnected(buffer: ByteBuffer?) {
        onTunnelEstablished()
    }

    override fun beforeSend(buffer: ByteBuffer?) {
        // TODO Auto-generated method stub
    }

    override fun afterReceived(buffer: ByteBuffer?) {
        // TODO Auto-generated method stub
    }

    override fun isTunnelEstablished(): Boolean {
        return true
    }

    override fun onDispose() {
        // TODO Auto-generated method stub
    }
}
