package com.akmobile.supervpn.proxy

data class ProxyGroupUI(
    var country: String,
    var collapsed: Boolean = true,
    var list: List<ProxyUI> = mutableListOf()
)