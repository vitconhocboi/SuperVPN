package com.hoangsinh.supervpn.network.dns

import java.nio.ByteBuffer

class DnsPacket {
    var Header: DnsHeader? = null
    var Questions: Array<Question?>? = null
    var Resources: Array<Resource?>? = null
    var AResources: Array<Resource?>? = null
    var EResources: Array<Resource?>? = null

    var Size: Int = 0

    fun ToBytes(buffer: ByteBuffer?) {
        Header!!.questionCount = 0
        Header!!.resourceCount = 0
        Header!!.aResourceCount = 0
        Header!!.eResourceCount = 0

        if (Questions != null) Header!!.questionCount = Questions!!.size.toShort()
        if (Resources != null) Header!!.resourceCount = Resources!!.size.toShort()
        if (AResources != null) Header!!.aResourceCount = AResources!!.size.toShort()
        if (EResources != null) Header!!.eResourceCount = EResources!!.size.toShort()

        Header!!.ToBytes(buffer!!)

        for (i in 0 until Header!!.questionCount) {
            Questions!![i]!!.ToBytes(buffer)
        }

        for (i in 0 until Header!!.resourceCount) {
            Resources!![i]!!.ToBytes(buffer)
        }

        for (i in 0 until Header!!.aResourceCount) {
            AResources!![i]!!.ToBytes(buffer)
        }

        for (i in 0 until Header!!.eResourceCount) {
            EResources!![i]!!.ToBytes(buffer)
        }
    }

    companion object {
        fun FromBytes(buffer: ByteBuffer): DnsPacket? {
            if (buffer.limit() < 12) return null
            if (buffer.limit() > 512) return null

            val packet = DnsPacket()
            packet.Size = buffer.limit()
            packet.Header = DnsHeader.FromBytes(buffer)

            if (packet.Header!!.questionCount > 2 || packet.Header!!.resourceCount > 50 || packet.Header!!.aResourceCount > 50 || packet.Header!!.eResourceCount > 50) {
                return null
            }

            packet.Questions = arrayOfNulls(packet.Header!!.questionCount.toInt())
            packet.Resources = arrayOfNulls(
                packet.Header!!.resourceCount.toInt()
            )
            packet.AResources = arrayOfNulls(
                packet.Header!!.aResourceCount.toInt()
            )
            packet.EResources = arrayOfNulls(
                packet.Header!!.eResourceCount.toInt()
            )

            for (i in packet.Questions!!.indices) {
                packet.Questions!![i] = Question.FromBytes(buffer)
            }

            for (i in packet.Resources!!.indices) {
                packet.Resources!![i] = Resource.FromBytes(buffer)
            }

            for (i in packet.AResources!!.indices) {
                packet.AResources!![i] = Resource.FromBytes(buffer)
            }

            for (i in packet.EResources!!.indices) {
                packet.EResources!![i] = Resource.FromBytes(buffer)
            }

            return packet
        }

        @JvmStatic
        fun ReadDomain(buffer: ByteBuffer, dnsHeaderOffset: Int): String {
            val sb = StringBuilder()
            var len = 0
            while (buffer.hasRemaining() && ((buffer.get().toInt() and 0xFF).also {
                    len = it
                }) > 0) {
                if ((len and 0xc0) == 0xc0) // pointer ��2λΪ11��ʾ��ָ�롣�磺1100 0000
                {
                    // ָ���ȡֵ��ǰһ�ֽڵĺ�6λ�Ӻ�һ�ֽڵ�8λ��14λ��ֵ��
                    var pointer = buffer.get().toInt() and 0xFF // ��8λ
                    pointer = pointer or ((len and 0x3F) shl 8) // ��6λ

                    val newBuffer = ByteBuffer.wrap(
                        buffer.array(),
                        dnsHeaderOffset + pointer,
                        dnsHeaderOffset + buffer.limit()
                    )
                    sb.append(ReadDomain(newBuffer, dnsHeaderOffset))
                    return sb.toString()
                } else {
                    while (len > 0 && buffer.hasRemaining()) {
                        sb.append((buffer.get().toInt() and 0xFF).toChar())
                        len--
                    }
                    sb.append('.')
                }
            }

            if (len == 0 && sb.length > 0) {
                sb.deleteCharAt(sb.length - 1) //ȥ��ĩβ�ĵ㣨.��
            }
            return sb.toString()
        }

        @JvmStatic
        fun WriteDomain(domain: String?, buffer: ByteBuffer) {
            if (domain == null || domain === "") {
                buffer.put(0.toByte())
                return
            }

            val arr = domain.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (item in arr) {
                if (arr.size > 1) {
                    buffer.put(item.length.toByte())
                }

                for (i in 0 until item.length) {
                    buffer.put(item.codePointAt(i).toByte())
                }
            }
        }
    }
}
