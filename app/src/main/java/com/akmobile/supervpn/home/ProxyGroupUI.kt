package com.akmobile.supervpn.home

data class ProxyGroupUI(
    var id: String, var group: String, var country: String, var collapsed: Boolean = true,
    var list: MutableList<ProxyUI> = mutableListOf()
)