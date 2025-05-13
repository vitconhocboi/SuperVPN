package com.akmobile.supervpn.proxy

data class ProxyGroupUI(
    var country: String,
    var active: Boolean = false,
    var list: List<ProxyUI> = mutableListOf()
)