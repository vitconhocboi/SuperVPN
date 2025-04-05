package com.akmobile.supervpn.settings.appproxy

import com.akmobile.supervpn.db.VpnAppItemDB


fun AppProxyUI.mapToDB(): VpnAppItemDB {
    return VpnAppItemDB(
        packageName, packageName
    )
}
