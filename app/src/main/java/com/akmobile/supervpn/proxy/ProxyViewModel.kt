package com.akmobile.supervpn.proxy

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akmobile.supervpn.home.HomeFragment
import com.akmobile.supervpn.network.LocalVpnService
import com.akmobile.supervpn.network.LocalVpnService.Companion.IsRunning
import com.common.baseui.ResultData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProxyViewModel @Inject constructor(
    private val proxyUseCase: ProxyInterface,
    private val proxyUpdate: ProxyConnection
) : ViewModel(), ISuperVpnProxyUpdate {
    val allProxy = MutableStateFlow<ResultData<List<ProxyGroupUI>>>(ResultData.standby())

    val isConnected = MutableLiveData(proxyUpdate.vpnState)

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
        proxyUpdate.setProxyUpdate(this)
    }

    fun stopProxy(context: Context?) {
        LocalVpnService.stopProxy(context!!)
    }

    override fun updateUI(status: String) {
        isConnected.postValue(status)
    }
}