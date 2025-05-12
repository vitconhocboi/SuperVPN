package com.akmobile.supervpn.settings.dns

data class DnsUI(
    val id: String,
    val name: String,
    val server: String,
    val icon: String,
    var active: Boolean = false
)