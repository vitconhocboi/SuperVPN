package com.tici.vpn.proxy.master.proxy

fun ProxyUI.mapToDB(): ProxyDB {
    return ProxyDB(
        id, type, name, host, port, username, password, country, active
    )
}

fun ProxyDB.mapToUI(): ProxyUI {
    return ProxyUI(
        id, type, name, host, port, username, password, country, active
    )
}