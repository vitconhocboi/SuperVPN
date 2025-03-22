package com.akmobile.supervpn.network.tcpip

import java.net.Inet4Address
import java.net.InetAddress
import java.net.UnknownHostException
import kotlin.experimental.inv

object CommonMethods {
    fun ipIntToInet4Address(ip: Int): InetAddress? {
        val ipAddress = ByteArray(4)
        writeInt(ipAddress, 0, ip)
        try {
            return Inet4Address.getByAddress(ipAddress)
        } catch (e: UnknownHostException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
            return null
        }
    }

    @JvmStatic
    fun ipIntToString(ip: Int): String {
        return String.format(
            "%s.%s.%s.%s", (ip shr 24) and 0x00FF,
            (ip shr 16) and 0x00FF, (ip shr 8) and 0x00FF, ip and 0x00FF
        )
    }

    fun ipBytesToString(ip: ByteArray): String {
        return String.format(
            "%s.%s.%s.%s",
            ip[0].toInt() and 0x00FF,
            ip[1].toInt() and 0x00FF,
            ip[2].toInt() and 0x00FF,
            ip[3].toInt() and 0x00FF
        )
    }

    fun ipStringToInt(ip: String): Int {
        val arrStrings = ip.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val r = ((arrStrings[0].toInt() shl 24)
                or (arrStrings[1].toInt() shl 16)
                or (arrStrings[2].toInt() shl 8)
                or arrStrings[3].toInt())
        return r
    }

    @JvmStatic
    fun readInt(data: ByteArray, offset: Int): Int {
        val r = (((data[offset].toInt() and 0xFF) shl 24)
                or ((data[offset + 1].toInt() and 0xFF) shl 16)
                or ((data[offset + 2].toInt() and 0xFF) shl 8) or (data[offset + 3].toInt() and 0xFF))
        return r
    }

    @JvmStatic
    fun readShort(data: ByteArray, offset: Int): Short {
        val r = ((data[offset].toInt() and 0xFF) shl 8) or (data[offset + 1].toInt() and 0xFF)
        return r.toShort()
    }

    @JvmStatic
    fun writeInt(data: ByteArray, offset: Int, value: Int) {
        data[offset] = (value shr 24).toByte()
        data[offset + 1] = (value shr 16).toByte()
        data[offset + 2] = (value shr 8).toByte()
        data[offset + 3] = value.toByte()
    }

    @JvmStatic
    fun writeShort(data: ByteArray, offset: Int, value: Short) {
        data[offset] = (value.toInt() shr 8).toByte()
        data[offset + 1] = value.toByte()
    }

    // �����ֽ�˳���������ֽ�˳���ת��
    fun htons(u: Short): Short {
        val r = ((u.toInt() and 0xFFFF) shl 8) or ((u.toInt() and 0xFFFF) shr 8)
        return r.toShort()
    }

    fun ntohs(u: Short): Short {
        val r = ((u.toInt() and 0xFFFF) shl 8) or ((u.toInt() and 0xFFFF) shr 8)
        return r.toShort()
    }

    fun hton(u: Int): Int {
        var r = (u shr 24) and 0x000000FF
        r = r or ((u shr 8) and 0x0000FF00)
        r = r or ((u shl 8) and 0x00FF0000)
        r = r or ((u shl 24) and -0x1000000)
        return r
    }

    fun ntoh(u: Int): Int {
        var r = (u shr 24) and 0x000000FF
        r = r or ((u shr 8) and 0x0000FF00)
        r = r or ((u shl 8) and 0x00FF0000)
        r = r or ((u shl 24) and -0x1000000)
        return r
    }

    fun checksum(sum: Long, buf: ByteArray, offset: Int, len: Int): Short {
        var sum = sum
        sum += getsum(buf, offset, len)
        while ((sum shr 16) > 0) sum = (sum and 0xFFFFL) + (sum shr 16)
        return sum.toShort().inv()
    }

    fun getsum(buf: ByteArray, offset: Int, len: Int): Long {
        var offset = offset
        var len = len
        var sum: Long = 0 /* assume 32 bit long, 16 bit short */
        while (len > 1) {
            sum += (readShort(buf, offset).toInt() and 0xFFFF).toLong()
            offset += 2
            len -= 2
        }

        if (len > 0) /* take care of left over byte */ {
            sum += ((buf[offset].toInt() and 0xFF) shl 8).toLong()
        }
        return sum
    }

    fun ComputeIPChecksum(ipHeader: IPHeader): Boolean {
        val oldCrc = ipHeader.crc
        ipHeader.crc = 0.toShort() // ����ǰ����
        val newCrc = checksum(
            0, ipHeader.m_Data,
            ipHeader.m_Offset, ipHeader.headerLength
        )
        ipHeader.crc = newCrc
        return oldCrc == newCrc
    }

    fun ComputeTCPChecksum(ipHeader: IPHeader, tcpHeader: TCPHeader): Boolean {
        ComputeIPChecksum(ipHeader) //����IPУ���
        val ipData_len = ipHeader.totalLength - ipHeader.headerLength // IP��ݳ���
        if (ipData_len < 0) return false
        var sum = getsum(
            ipHeader.m_Data, ipHeader.m_Offset
                    + IPHeader.offset_src_ip, 8
        )
        sum += (ipHeader.protocol.toInt() and 0xFF).toLong()
        sum += ipData_len.toLong()

        val oldCrc = tcpHeader.crc
        tcpHeader.crc = 0.toShort() // ����ǰ��0

        val newCrc = checksum(sum, tcpHeader.m_Data, tcpHeader.m_Offset, ipData_len) // ����У���

        tcpHeader.crc = newCrc
        return oldCrc == newCrc
    }

    fun ComputeUDPChecksum(ipHeader: IPHeader, udpHeader: UDPHeader): Boolean {
        ComputeIPChecksum(ipHeader) //����IPУ���
        val ipData_len = ipHeader.totalLength - ipHeader.headerLength // IP��ݳ���
        if (ipData_len < 0) return false
        // ����Ϊα�ײ���
        var sum = getsum(
            ipHeader.m_Data, ipHeader.m_Offset
                    + IPHeader.offset_src_ip, 8
        )
        sum += (ipHeader.protocol.toInt() and 0xFF).toLong()
        sum += ipData_len.toLong()

        val oldCrc = udpHeader.crc
        udpHeader.crc = 0.toShort() // ����ǰ��0

        val newCrc = checksum(sum, udpHeader.m_Data, udpHeader.m_Offset, ipData_len) // ����У���

        udpHeader.crc = newCrc
        return oldCrc == newCrc
    }
}
