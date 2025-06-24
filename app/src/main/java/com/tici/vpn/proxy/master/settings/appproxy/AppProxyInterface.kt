package com.tici.vpn.proxy.master.settings.appproxy

import com.tici.vpn.proxy.master.db.VpnAppItemDB
import com.common.baseui.ResultData
import kotlinx.coroutines.flow.Flow

interface AppProxyInterface {
    fun getAllowApp(): Flow<ResultData<List<VpnAppItemDB>>>

    fun setAllowApp(item: AppProxyUI): Flow<ResultData<Boolean>>

    fun setAllowApps(item: ArrayList<AppProxyUI>): Flow<ResultData<Boolean>>
}