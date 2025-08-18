package com.tici.vpn.proxy.master.api

import okhttp3.Dns
import java.net.Inet4Address
import java.net.InetAddress
import java.net.UnknownHostException

object IPv4OnlyDns : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        return try {
            InetAddress.getAllByName(hostname)
                .filterIsInstance<Inet4Address>()
                .ifEmpty {
                    throw UnknownHostException("No IPv4 address found for $hostname")
                }
        } catch (e: Exception) {
            throw UnknownHostException("Failed to resolve $hostname: ${e.message}")
        }
    }
}