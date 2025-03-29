package com.akmobile.supervpn.proxy

import androidx.lifecycle.ViewModel
import com.common.baseui.ResultData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ProxyViewModel @Inject constructor(private val proxyUseCase: ProxyInterface) : ViewModel() {
    fun getAllProxy(): Flow<ResultData<List<ProxyGroupUI>>> {
        return proxyUseCase.getAllProxy()
    }

    fun getActiveProxy(): Flow<ResultData<ProxyUI?>> {
        return proxyUseCase.getActiveProxy()
    }

    fun setActiveProxy(id: String) {
        proxyUseCase.setActiveProxy(id)
    }
}