package com.akmobile.supervpn.network.dns

class DnsFlags {
    var QR: Boolean = false //1 bits
    var OpCode: Int = 0 //4 bits
    var AA: Boolean = false //1 bits
    var TC: Boolean = false //1 bits
    var RD: Boolean = false //1 bits
    var RA: Boolean = false //1 bits
    var Zero: Int = 0 //3 bits
    var Rcode: Int = 0 //4 bits

    fun ToShort(): Short {
        var m_Flags = 0
        m_Flags = m_Flags or ((if (this.QR) 1 else 0) shl 7)
        m_Flags = m_Flags or ((this.OpCode and 0x0F) shl 3)
        m_Flags = m_Flags or ((if (this.AA) 1 else 0) shl 2)
        m_Flags = m_Flags or ((if (this.TC) 1 else 0) shl 1)
        m_Flags = m_Flags or if (this.RD) 1 else 0
        m_Flags = m_Flags or ((if (this.RA) 1 else 0) shl 15)
        m_Flags = m_Flags or ((this.Zero and 0x07) shl 12)
        m_Flags = m_Flags or ((this.Rcode and 0x0F) shl 8)
        return m_Flags.toShort()
    }

    companion object {
        fun Parse(value: Short): DnsFlags {
            val m_Flags = value.toInt() and 0xFFFF
            val flags = DnsFlags()
            flags.QR = ((m_Flags shr 7) and 0x01) == 1
            flags.OpCode = (m_Flags shr 3) and 0x0F
            flags.AA = ((m_Flags shr 2) and 0x01) == 1
            flags.TC = ((m_Flags shr 1) and 0x01) == 1
            flags.RD = (m_Flags and 0x01) == 1
            flags.RA = (m_Flags shr 15) == 1
            flags.Zero = (m_Flags shr 12) and 0x07
            flags.Rcode = ((m_Flags shr 8) and 0xF)
            return flags
        }
    }
}
