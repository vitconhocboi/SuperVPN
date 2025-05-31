package com.highsecure.vpn.proxy.master.network.stun

import com.highsecure.vpn.proxy.master.network.tcpip.CommonMethods.readInt
import com.highsecure.vpn.proxy.master.network.tcpip.CommonMethods.readShort
import com.highsecure.vpn.proxy.master.network.tcpip.CommonMethods.writeInt
import com.highsecure.vpn.proxy.master.network.tcpip.CommonMethods.writeShort
import java.util.Locale

class StunAttr(var m_Data: ByteArray, var m_Offset: Int) {
    
    var type: Int
        get() = readShort(m_Data, m_Offset + offset_type).toInt() and 0xFFFF
        set(value) {
            writeShort(m_Data, m_Offset + offset_type, value.toShort())
        }

    var length: Int
        get() = readShort(m_Data, m_Offset + offset_length).toInt() and 0xFFFF
        set(value) {
            writeShort(m_Data, m_Offset + offset_length, value.toShort())
        }

    var reserve: Int
        get() = m_Data[m_Offset + offset_reserve].toInt() and 0xFF
        set(value) {
            m_Data[m_Offset + offset_reserve] = value.toByte()
        }

    var proto: Int
        get() = m_Data[m_Offset + offset_proto].toInt() and 0xFF
        set(value) {
            m_Data[m_Offset + offset_proto] = value.toByte()
        }

    var port: Int
        get() = readShort(m_Data, m_Offset + offset_port).toInt() and 0xFFFF
        set(value) {
            writeShort(m_Data, m_Offset + offset_port, value.toShort())
        }

    var ip: Int
        get() = readInt(m_Data, m_Offset + offset_ip)
        set(value) {
            writeInt(m_Data, m_Offset + offset_ip, value)
        }

    override fun toString(): String {
        return String.format(
            Locale.ENGLISH,
            "STUN Attr: Type=0x%04X, Length=%d, Proto=%d, Port=%d, IP=%d.%d.%d.%d",
            type,
            length,
            proto,
            port,
            (ip shr 24) and 0xFF,
            (ip shr 16) and 0xFF,
            (ip shr 8) and 0xFF,
            ip and 0xFF
        )
    }

    companion object {
        const val ATTR_HEADER_LENGTH: Int = 4 // Type(2) + Length(2)
        const val MAPPED_ADDRESS_LENGTH: Int = 8 // Reserve(1) + Proto(1) + Port(2) + IP(4)
        
        // Common STUN attribute types
        const val MAPPED_ADDRESS: Int = 0x0001
        const val XOR_MAPPED_ADDRESS: Int = 0x0020
        const val USERNAME: Int = 0x0006
        const val MESSAGE_INTEGRITY: Int = 0x0008
        const val FINGERPRINT: Int = 0x8028
        
        // Field offsets
        const val offset_type: Int = 0 // Attribute type (2 bytes)
        const val offset_length: Int = 2 // Attribute length (2 bytes)
        const val offset_reserve: Int = 4 // Reserved field (1 byte)
        const val offset_proto: Int = 5 // Protocol (1 byte)
        const val offset_port: Int = 6 // Port (2 bytes)
        const val offset_ip: Int = 8 // IP address (4 bytes)
    }
} 