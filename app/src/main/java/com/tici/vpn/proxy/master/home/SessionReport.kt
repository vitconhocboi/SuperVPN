package com.tici.vpn.proxy.master.home

/** Summary of one VPN session, shown on the disconnected screen. */
data class SessionReport(
    var duration: String = "",
    var upload: String = "",
    var download: String = ""
)
