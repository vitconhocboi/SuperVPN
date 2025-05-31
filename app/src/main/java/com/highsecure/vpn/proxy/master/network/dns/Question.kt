package com.highsecure.vpn.proxy.master.network.dns

import com.highsecure.vpn.proxy.master.network.dns.DnsPacket.Companion.ReadDomain
import com.highsecure.vpn.proxy.master.network.dns.DnsPacket.Companion.WriteDomain
import java.nio.ByteBuffer

class Question {
    var Domain: String? = null
    var Type: Short = 0
    var Class: Short = 0

    private var offset = 0
    private var length = 0

    fun Offset(): Int {
        return offset
    }

    fun Length(): Int {
        return length
    }

    fun ToBytes(buffer: ByteBuffer) {
        this.offset = buffer.position()
        WriteDomain(this.Domain, buffer)
        buffer.putShort(this.Type)
        buffer.putShort(this.Class)
        this.length = buffer.position() - this.offset
    }

    companion object {
        fun FromBytes(buffer: ByteBuffer): Question {
            val q = Question()
            q.offset = buffer.arrayOffset() + buffer.position()
            q.Domain = ReadDomain(buffer, buffer.arrayOffset())
            q.Type = buffer.getShort()
            q.Class = buffer.getShort()
            q.length = buffer.arrayOffset() + buffer.position() - q.offset
            return q
        }
    }
}
