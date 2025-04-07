package com.akmobile.supervpn.settings.appproxy

import com.akmobile.supervpn.db.VpnAppItemDB
import com.common.baseui.ResultData
import kotlinx.coroutines.flow.Flow

interface AppProxyInterface {
    fun getAllowApp(): Flow<ResultData<List<VpnAppItemDB>>>

    fun setAllowApp(item: AppProxyUI): Flow<ResultData<Boolean>>
}