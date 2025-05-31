package com.highsecure.vpn.proxy.master.settings.appproxy

import com.highsecure.vpn.proxy.master.db.VpnAppItemDB


fun AppProxyUI.mapToDB(): VpnAppItemDB {
    return VpnAppItemDB(
        packageName, packageName
    )
}
