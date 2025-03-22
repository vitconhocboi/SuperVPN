package com.akmobile.supervpn.network.tcpip

import com.akmobile.supervpn.network.tcpip.CommonMethods.ipIntToString
import com.akmobile.supervpn.network.tcpip.CommonMethods.readInt
import com.akmobile.supervpn.network.tcpip.CommonMethods.readShort
import com.akmobile.supervpn.network.tcpip.CommonMethods.writeInt
import com.akmobile.supervpn.network.tcpip.CommonMethods.writeShort
import java.util.Locale

class IPHeader(var m_Data: ByteArray, var m_Offset: Int) {
    fun Default() {
        headerLength = 20
        tos = 0.toByte()
        totalLength = 0
        identification = 0
        flagsAndOffset = 0.toShort()
        tTL = 64.toByte()
    }

    val dataLength: Int
        get() = this.totalLength - this.headerLength

    var headerLength: Int
        get() = (m_Data[m_Offset + offset_ver_ihl].toInt() and 0x0F) * 4
        set(value) {
            m_Data[m_Offset + offset_ver_ihl] = ((4 shl 4) or (value / 4)).toByte()
        }

    var tos: Byte
        get() = m_Data[m_Offset + offset_tos]
        set(value) {
            m_Data[m_Offset + offset_tos] = value
        }

    var totalLength: Int
        get() = readShort(m_Data, m_Offset + offset_tlen).toInt() and 0xFFFF
        set(value) {
            writeShort(m_Data, m_Offset + offset_tlen, value.toShort())
        }

    var identification: Int
        get() = readShort(m_Data, m_Offset + offset_identification).toInt() and 0xFFFF
        set(value) {
            writeShort(m_Data, m_Offset + offset_identification, value.toShort())
        }

    var flagsAndOffset: Short
        get() = readShort(m_Data, m_Offset + offset_flags_fo)
        set(value) {
            writeShort(m_Data, m_Offset + offset_flags_fo, value)
        }

    var tTL: Byte
        get() = m_Data[m_Offset + offset_ttl]
        set(value) {
            m_Data[m_Offset + offset_ttl] = value
        }

    var protocol: Byte
        get() = m_Data[m_Offset + offset_proto]
        set(value) {
            m_Data[m_Offset + offset_proto] = value
        }

    var crc: Short
        get() = readShort(m_Data, m_Offset + offset_crc)
        set(value) {
            writeShort(m_Data, m_Offset + offset_crc, value)
        }

    var sourceIP: Int
        get() = readInt(m_Data, m_Offset + offset_src_ip)
        set(value) {
            writeInt(m_Data, m_Offset + offset_src_ip, value)
        }

    var destinationIP: Int
        get() = readInt(m_Data, m_Offset + offset_dest_ip)
        set(value) {
            writeInt(m_Data, m_Offset + offset_dest_ip, value)
        }

    override fun toString(): String {
        return String.format(
            Locale.ENGLISH, "%s->%s Pro=%s,HLen=%d", ipIntToString(
                sourceIP
            ), ipIntToString(destinationIP), protocol, headerLength
        )
    }

    companion object {
        const val IP: Short = 0x0800
        const val ICMP: Byte = 1
        const val TCP: Byte = 6
        const val UDP: Byte = 17
        const val offset_proto: Byte = 9 // 9: Protocol
        const val offset_src_ip: Int = 12 // 12: Source address
        const val offset_dest_ip: Int = 16 // 16: Destination address
        const val offset_ver_ihl: Byte =
            0 // 0: Version (4 bits) + Internet header length (4// bits)
        const val offset_tos: Byte = 1 // 1: Type of service
        const val offset_tlen: Short = 2 // 2: Total length
        const val offset_identification: Short = 4 // :4 Identification
        const val offset_flags_fo: Short = 6 // 6: Flags (3 bits) + Fragment offset (13 bits)
        const val offset_ttl: Byte = 8 // 8: Time to live
        const val offset_crc: Short = 10 // 10: Header checksum
        const val offset_op_pad: Int = 20 // 20: Option + Padding
    }
}
