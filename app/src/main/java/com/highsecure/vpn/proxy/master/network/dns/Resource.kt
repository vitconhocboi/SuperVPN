package com.highsecure.vpn.proxy.master.network.dns

import com.highsecure.vpn.proxy.master.network.dns.DnsPacket.Companion.ReadDomain
import com.highsecure.vpn.proxy.master.network.dns.DnsPacket.Companion.WriteDomain
import java.nio.ByteBuffer

class Resource {
    var Domain: String? = null
    var Type: Short = 0
    var Class: Short = 0
    var TTL: Int = 0
    var DataLength: Short = 0
    var Data: ByteArray? = null

    private var offset = 0
    private var length = 0

    fun Offset(): Int {
        return offset
    }

    fun Length(): Int {
        return length
    }

    fun ToBytes(buffer: ByteBuffer) {
        if (this.Data == null) {
            this.Data = ByteArray(0)
        }
        this.DataLength = Data!!.size.toShort()

        this.offset = buffer.position()
        WriteDomain(this.Domain, buffer)
        buffer.putShort(this.Type)
        buffer.putShort(this.Class)
        buffer.putInt(this.TTL)

        buffer.putShort(this.DataLength)
        buffer.put(this.Data)
        this.length = buffer.position() - this.offset
    }


    companion object {
        fun FromBytes(buffer: ByteBuffer): Resource {
            val r = Resource()
            r.offset = buffer.arrayOffset() + buffer.position()
            r.Domain = ReadDomain(buffer, buffer.arrayOffset())
            r.Type = buffer.getShort()
            r.Class = buffer.getShort()
            r.TTL = buffer.getInt()
            r.DataLength = buffer.getShort()
            r.Data = ByteArray(r.DataLength.toInt() and 0xFFFF)
            buffer[r.Data]
            r.length = buffer.arrayOffset() + buffer.position() - r.offset
            return r
        }
    }
}
