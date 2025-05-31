package com.highsecure.vpn.proxy.master.network.dns

import com.highsecure.vpn.proxy.master.network.tcpip.CommonMethods

class ResourcePointer(var Data: ByteArray, var Offset: Int) {
    fun setDomain(value: Short) {
        CommonMethods.writeShort(Data, Offset + offset_Domain, value)
    }

    var type: Short
        get() = CommonMethods.readShort(Data, Offset + offset_Type)
        set(value) {
            CommonMethods.writeShort(Data, Offset + offset_Type, value)
        }

    fun getClass(value: Short): Short {
        return CommonMethods.readShort(Data, Offset + offset_Class)
    }

    fun setClass(value: Short) {
        CommonMethods.writeShort(Data, Offset + offset_Class, value)
    }

    var tTL: Int
        get() = CommonMethods.readInt(Data, Offset + offset_TTL)
        set(value) {
            CommonMethods.writeInt(Data, Offset + offset_TTL, value)
        }

    var dataLength: Short
        get() = CommonMethods.readShort(Data, Offset + offset_DataLength)
        set(value) {
            CommonMethods.writeShort(Data, Offset + offset_DataLength, value)
        }

    var iP: Int
        get() = CommonMethods.readInt(Data, Offset + offset_IP)
        set(value) {
            CommonMethods.writeInt(Data, Offset + offset_IP, value)
        }

    companion object {
        const val offset_Domain: Short = 0
        const val offset_Type: Short = 2
        const val offset_Class: Short = 4
        const val offset_TTL: Int = 6
        const val offset_DataLength: Short = 10
        const val offset_IP: Int = 12
    }
}
