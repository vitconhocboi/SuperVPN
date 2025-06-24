package com.tici.vpn.proxy.master.remoteconfig

interface ConfigLoader {
    fun fetch(onComplete: () -> Unit)
}