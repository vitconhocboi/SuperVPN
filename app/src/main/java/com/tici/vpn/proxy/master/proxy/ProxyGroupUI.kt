package com.tici.vpn.proxy.master.proxy

data class ProxyGroupUI(
    var country: String,
    var active: Boolean = false,
    var list: List<ProxyUI> = mutableListOf(),
//    var isQuickAccess: Boolean = false,
    var type: String,
    var proxy_group: String? = "all"
)