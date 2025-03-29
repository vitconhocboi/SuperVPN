package com.akmobile.supervpn.proxy


data class ProxyUI(
    val id: String,
    val type: String,
    val name: String,
    val host: String,
    val port: String,
    val username: String,
    val password: String,
    val country: String,
    var active: Boolean = false
)