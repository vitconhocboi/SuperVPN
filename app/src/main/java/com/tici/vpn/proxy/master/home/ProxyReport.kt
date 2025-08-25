package com.tici.vpn.proxy.master.home

import com.tici.vpn.proxy.master.network.ProxySpeedTest

data class ProxyReport(
    var duration: String = "",
    var upload: String = "",
    var download: String = "",
    var proxyConfig: ProxySpeedTest.ProxyConfig? = null

)