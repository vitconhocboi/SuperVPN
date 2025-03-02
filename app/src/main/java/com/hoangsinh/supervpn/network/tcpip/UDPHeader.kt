package com.hoangsinh.supervpn.network.tcpip

import com.hoangsinh.supervpn.network.tcpip.CommonMethods.readShort
import com.hoangsinh.supervpn.network.tcpip.CommonMethods.writeShort
import java.util.Locale

class UDPHeader(var m_Data: ByteArray, var m_Offset: Int) {
    var sourcePort: Short
        get() = readShort(m_Data, m_Offset + offset_src_port)
        set(value) {
            writeShort(m_Data, m_Offset + offset_src_port, value)
        }

    var destinationPort: Short
        get() = readShort(m_Data, m_Offset + offset_dest_port)
        set(value) {
            writeShort(m_Data, m_Offset + offset_dest_port, value)
        }

    var totalLength: Int
        get() = readShort(m_Data, m_Offset + offset_tlen).toInt() and 0xFFFF
        set(value) {
            writeShort(m_Data, m_Offset + offset_tlen, value.toShort())
        }

    var crc: Short
        get() = readShort(m_Data, m_Offset + offset_crc)
        set(value) {
            writeShort(m_Data, m_Offset + offset_crc, value)
        }

    override fun toString(): String {
        // TODO Auto-generated method stub
        return String.format(
            Locale.ENGLISH, "%d->%d", sourcePort.toInt() and 0xFFFF,
            destinationPort.toInt() and 0xFFFF
        )
    }

    companion object {
        const val offset_src_port: Short = 0 // Source port
        const val offset_dest_port: Short = 2 // Destination port
        const val offset_tlen: Short = 4 // Datagram length
        const val offset_crc: Short = 6 // Checksum
    }
}
