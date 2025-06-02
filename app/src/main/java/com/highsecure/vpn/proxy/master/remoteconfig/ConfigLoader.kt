package com.highsecure.vpn.proxy.master.remoteconfig

interface ConfigLoader {
    fun fetch(onComplete: () -> Unit)
}