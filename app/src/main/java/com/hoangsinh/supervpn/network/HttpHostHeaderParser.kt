package com.hoangsinh.supervpn.network

import android.util.Log
import com.hoangsinh.supervpn.network.tcpip.CommonMethods
import com.hoangsinh.supervpn.utils.Constant
import kotlin.experimental.and

object HttpHostHeaderParser {
    fun parseHost(buffer: ByteArray, offset: Int, count: Int): String? {
        try {
            when (buffer[offset]) {
                'G'.code.toByte(), 'H'.code.toByte(), 'P'.code.toByte(), 'D'.code.toByte(), 'O'.code.toByte(), 'T'.code.toByte(), 'C'.code.toByte() -> return getHttpHost(
                    buffer,
                    offset,
                    count
                )

                0x16.toByte() -> return getSNI(buffer, offset, count)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LocalVpnService.Instance.writeLog("Error: parseHost:%s", e)
        }
        return null
    }

    fun getHttpHost(buffer: ByteArray?, offset: Int, count: Int): String? {
        val headerString = String(buffer!!, offset, count)
        val headerLines =
            headerString.split("\\r\\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val requestLine = headerLines[0]
        if (requestLine.startsWith("GET") || requestLine.startsWith("POST") || requestLine.startsWith(
                "HEAD"
            ) || requestLine.startsWith("OPTIONS")
        ) {
            for (i in 1 until headerLines.size) {
                val nameValueStrings =
                    headerLines[i].split(":".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                if (nameValueStrings.size == 2) {
                    val name = nameValueStrings[0].lowercase().trim { it <= ' ' }
                    val value = nameValueStrings[1].trim { it <= ' ' }
                    if ("host" == name) {
                        return value
                    }
                }
            }
        }
        return null
    }

    fun getSNI(buffer: ByteArray, offset: Int, count: Int): String? {
        var offset = offset
        val limit = offset + count
        if (count > 43 && buffer[offset].toInt() == 0x16) { //TLS Client Hello
            offset += 43 //skip 43 bytes header

            //read sessionID:
            if (offset + 1 > limit) return null
            val sessionIDLength = buffer[offset++].toInt() and 0xFF
            offset += sessionIDLength

            //read cipher suites:
            if (offset + 2 > limit) return null
            val cipherSuitesLength: Int =
                (CommonMethods.readShort(buffer, offset) and 0xFFFF.toShort()).toInt()
            offset += 2
            offset += cipherSuitesLength

            //read Compression method:
            if (offset + 1 > limit) return null
            val compressionMethodLength = buffer[offset++].toInt() and 0xFF
            offset += compressionMethodLength

            if (offset == limit) {
                System.err.println("TLS Client Hello packet doesn't contains SNI info.(offset == limit)")
                return null
            }

            //read Extensions:
            if (offset + 2 > limit) return null
            val extensionsLength: Int =
                (CommonMethods.readShort(buffer, offset) and 0xFFFF.toShort()).toInt()
            offset += 2

            if (offset + extensionsLength > limit) {
                System.err.println("TLS Client Hello packet is incomplete.")
                return null
            }

            while (offset + 4 <= limit) {
                val type0 = buffer[offset++].toInt() and 0xFF
                val type1 = buffer[offset++].toInt() and 0xFF
                var length: Int =
                    (CommonMethods.readShort(buffer, offset) and 0xFFFF.toShort()).toInt()
                offset += 2

                if (type0 == 0x00 && type1 == 0x00 && length > 5) { //have SNI
                    offset += 5 //skip SNI header.
                    length -= 5 //SNI size;
                    if (offset + length > limit) return null
                    val serverName = String(buffer, offset, length)
                    if (ProxyConfig.IS_DEBUG) Log.d(Constant.TAG, "SNI: $serverName")
                    return serverName
                } else {
                    offset += length
                }
            }

            System.err.println("TLS Client Hello packet doesn't contains Host field info.")
            return null
        } else {
            System.err.println("Bad TLS Client Hello packet.")
            return null
        }
    }
}
