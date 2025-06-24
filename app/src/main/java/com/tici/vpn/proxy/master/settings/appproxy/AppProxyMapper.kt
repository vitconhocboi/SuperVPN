package com.tici.vpn.proxy.master.settings.appproxy

import com.tici.vpn.proxy.master.db.VpnAppItemDB


fun AppProxyUI.mapToDB(): VpnAppItemDB {
    return VpnAppItemDB(
        packageName, packageName
    )
}
