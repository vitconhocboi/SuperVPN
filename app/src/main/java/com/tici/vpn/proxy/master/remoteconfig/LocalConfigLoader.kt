package com.tici.vpn.proxy.master.remoteconfig

class LocalConfigLoader: ConfigLoader {
    override fun fetch(onComplete: () -> Unit) {
        onComplete.invoke()
    }
}
