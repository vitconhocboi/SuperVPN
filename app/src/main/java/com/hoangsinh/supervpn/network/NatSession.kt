package com.hoangsinh.supervpn.network

class NatSession {
    var RemoteIP: Int = 0
    var RemotePort: Short = 0
    var RemoteHost: String? = null
    var BytesSent: Int = 0
    var PacketSent: Int = 0
    var LastNanoTime: Long = 0
}
