package com.akmobile.supervpn.proxy

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akmobile.supervpn.network.LocalVpnService
import com.common.baseui.ResultData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProxyViewModel @Inject constructor(private val proxyUseCase: ProxyInterface) : ViewModel() {
    val allProxy = MutableStateFlow<ResultData<List<ProxyGroupUI>>>(ResultData.standby())

    fun getAllProxy() {
        viewModelScope.launch(Dispatchers.IO) {
            proxyUseCase.getAllProxy().collect { allProxy.value = it }
        }
    }

    val activeProxy = MutableStateFlow<ResultData<ProxyUI?>>(ResultData.standby())

    fun getActiveProxy() {
        viewModelScope.launch(Dispatchers.IO) {
            proxyUseCase.getActiveProxy().collect { activeProxy.value = it }
        }
    }

    fun setActiveProxy(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            proxyUseCase.setActiveProxy(id)
        }
    }

    fun startProxy(context: Context?) {
        LocalVpnService.startProxy(context!!)
    }

    fun stopProxy(context: Context?) {
        LocalVpnService.stopProxy(context!!)
    }
}