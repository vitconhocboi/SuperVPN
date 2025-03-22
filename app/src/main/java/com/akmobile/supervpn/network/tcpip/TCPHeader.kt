package com.akmobile.supervpn.network.tcpip

import com.akmobile.supervpn.network.tcpip.CommonMethods.readInt
import com.akmobile.supervpn.network.tcpip.CommonMethods.readShort
import com.akmobile.supervpn.network.tcpip.CommonMethods.writeShort
import java.util.Locale

class TCPHeader(var m_Data: ByteArray, var m_Offset: Int) {
    val headerLength: Int
        get() {
            val lenres = m_Data[m_Offset + offset_lenres].toInt() and 0xFF
            return (lenres shr 4) * 4
        }

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

    val flags: Byte
        get() = m_Data[m_Offset + offset_flag]

    var crc: Short
        get() = readShort(m_Data, m_Offset + offset_crc)
        set(value) {
            writeShort(m_Data, m_Offset + offset_crc, value)
        }

    val seqID: Int
        get() = readInt(m_Data, m_Offset + offset_seq)

    val ackID: Int
        get() = readInt(m_Data, m_Offset + offset_ack)

    override fun toString(): String {
        // TODO Auto-generated method stub
        return String.format(
            Locale.ENGLISH, "%s%s%s%s%s%s%d->%d %s:%s",
            if ((flags.toInt() and SYN) == SYN) "SYN " else "",
            if ((flags.toInt() and ACK) == ACK) "ACK " else "",
            if ((flags.toInt() and PSH) == PSH) "PSH " else "",
            if ((flags.toInt() and RST) == RST) "RST " else "",
            if ((flags.toInt() and FIN) == FIN) "FIN " else "",
            if ((flags.toInt() and URG) == URG) "URG " else "",
            sourcePort.toInt() and 0xFFFF,
            destinationPort.toInt() and 0xFFFF,
            seqID,
            ackID
        )
    }

    companion object {
        const val FIN: Int = 1
        const val SYN: Int = 2
        const val RST: Int = 4
        const val PSH: Int = 8
        const val ACK: Int = 16
        const val URG: Int = 32

        const val offset_src_port: Short = 0
        const val offset_dest_port: Short = 2
        const val offset_seq: Int = 4
        const val offset_ack: Int = 8
        const val offset_lenres: Byte = 12
        const val offset_flag: Byte = 13
        const val offset_win: Short = 14
        const val offset_crc: Short = 16
        const val offset_urp: Short = 18
    }
}
