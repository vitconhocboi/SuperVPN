package com.hoangsinh.supervpn.network.dns

import com.hoangsinh.supervpn.network.tcpip.CommonMethods
import java.nio.ByteBuffer

class DnsHeader(private var Data: ByteArray, private var Offset: Int) {
    var id: Short
        get() = CommonMethods.readShort(Data, Offset + offset_ID)
        set(value) = CommonMethods.writeShort(Data, Offset + offset_ID, value)

    var flags: DnsFlags? = null

    var questionCount: Short
        get() = CommonMethods.readShort(Data, Offset + offset_QuestionCount)
        set(value) = CommonMethods.writeShort(Data, Offset + offset_QuestionCount, value)

    var resourceCount: Short
        get() = CommonMethods.readShort(Data, Offset + offset_ResourceCount)
        set(value) = CommonMethods.writeShort(Data, Offset + offset_ResourceCount, value)

    var aResourceCount: Short
        get() = CommonMethods.readShort(Data, Offset + offset_AResourceCount)
        set(value) = CommonMethods.writeShort(Data, Offset + offset_AResourceCount, value)

    var eResourceCount: Short
        get() = CommonMethods.readShort(Data, Offset + offset_EResourceCount)
        set(value) = CommonMethods.writeShort(Data, Offset + offset_EResourceCount, value)

    fun ToBytes(buffer: ByteBuffer) {
        buffer.putShort(this.id)
        buffer.putShort(flags!!.ToShort())
        buffer.putShort(this.questionCount)
        buffer.putShort(this.resourceCount)
        buffer.putShort(this.aResourceCount)
        buffer.putShort(this.eResourceCount)
    }

    companion object {
        const val offset_ID: Short = 0
        const val offset_Flags: Short = 2
        const val offset_QuestionCount: Short = 4
        const val offset_ResourceCount: Short = 6
        const val offset_AResourceCount: Short = 8
        const val offset_EResourceCount: Short = 10

        @JvmStatic
        fun FromBytes(buffer: ByteBuffer): DnsHeader {
            val header = DnsHeader(buffer.array(), buffer.arrayOffset() + buffer.position())
            header.id = buffer.getShort()
            header.flags = DnsFlags.Parse(buffer.getShort())
            header.questionCount = buffer.getShort()
            header.resourceCount = buffer.getShort()
            header.aResourceCount = buffer.getShort()
            header.eResourceCount = buffer.getShort()
            return header
        }
    }
}
