package com.highsecure.vpn.proxy.master.network.stun

import com.highsecure.vpn.proxy.master.network.tcpip.CommonMethods.readInt
import com.highsecure.vpn.proxy.master.network.tcpip.CommonMethods.readShort
import com.highsecure.vpn.proxy.master.network.tcpip.CommonMethods.writeInt
import com.highsecure.vpn.proxy.master.network.tcpip.CommonMethods.writeShort
import java.util.Locale

class StunHeader(var m_Data: ByteArray, var m_Offset: Int) {
    
    var type: Short
        get() = readShort(m_Data, m_Offset + offset_type)
        set(value) {
            writeShort(m_Data, m_Offset + offset_type, value)
        }

    var length: Short
        get() = readShort(m_Data, m_Offset + offset_length)
        set(value) {
            writeShort(m_Data, m_Offset + offset_length, value)
        }

    var cookies: Int
        get() = readInt(m_Data, m_Offset + offset_cookies)
        set(value) {
            writeInt(m_Data, m_Offset + offset_cookies, value)
        }

    fun getTransactionId(): ByteArray {
        val transId = ByteArray(TRANSACTION_ID_LENGTH)
        System.arraycopy(m_Data, m_Offset + offset_transaction_id, transId, 0, TRANSACTION_ID_LENGTH)
        return transId
    }

    fun setTransactionId(transactionId: ByteArray) {
        require(transactionId.size == TRANSACTION_ID_LENGTH) { "Transaction ID must be exactly $TRANSACTION_ID_LENGTH bytes" }
        System.arraycopy(transactionId, 0, m_Data, m_Offset + offset_transaction_id, TRANSACTION_ID_LENGTH)
    }

    override fun toString(): String {
        return String.format(
            Locale.ENGLISH,
            "STUN Header: Type=0x%04X, Length=%d",
            type.toInt() and 0xFFFF,
            length.toInt() and 0xFFFF
        )
    }

    companion object {
        const val HEADER_LENGTH: Int = 20 // Total header length in bytes
        const val TRANSACTION_ID_LENGTH: Int = 12 // Length of transaction ID in bytes
        const val MAGIC_COOKIE: Int = 0x2112A442 // Standard STUN magic cookie value

        const val offset_type: Int = 0 // Message type (2 bytes)
        const val offset_length: Int = 2 // Message length (2 bytes)
        const val offset_cookies: Int = 4 // Magic cookie (4 bytes)
        const val offset_transaction_id: Int = 8 // Transaction ID (12 bytes)
    }
} 