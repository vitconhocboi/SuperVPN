package com.highsecure.vpn.proxy.master.proxy

import com.common.baseui.ResultData
import kotlinx.coroutines.flow.Flow

interface ProxyInterface {
    fun getAllProxy(): Flow<ResultData<List<ProxyGroupUI>>>

    fun getActiveProxy(): Flow<ResultData<ProxyUI?>>

    fun setActiveProxy(id: String): Flow<ResultData<Boolean>>
}