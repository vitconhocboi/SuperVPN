package com.highsecure.vpn.proxy.master.proxy

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.common.baseui.BaseAppConfig
import com.common.baseui.ResultData
import com.highsecure.vpn.proxy.master.api.ApiService
import com.highsecure.vpn.proxy.master.api.DisconnectRequest
import com.highsecure.vpn.proxy.master.api.ProxyRequest
import com.highsecure.vpn.proxy.master.network.LocalVpnService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProxyViewModel @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@Inject constructor(
    private val proxyUseCase: ProxyInterface,
    private val proxyUpdate: ProxyConnection,
    private val apiService: ApiService  // Add API service injection
) : ViewModel(), ISuperVpnProxyUpdate {
    val allFreeProxy =
        MutableStateFlow<ResultData<List<ProxyGroupUI>>>(ResultData.Companion.standby())

    val allPremiumProxy =
        MutableStateFlow<ResultData<List<ProxyGroupUI>>>(ResultData.Companion.standby())

    val isConnected = MutableLiveData(proxyUpdate.vpnState)

    fun getAllFreeProxy(context: Context, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getCountries(type)
                if (response.isSuccessful) {
                    response.body()?.let { countriesResponse ->
                        val listProxies = countriesResponse.countries.map { country ->
                            ProxyGroupUI(
                                country = country, active = country == BaseAppConfig.proxyCountry
                            )
                        }
                        allFreeProxy.emit(ResultData.Companion.success(listProxies))
                    }
                }
            } catch (e: Exception) {
                // Handle error case
                allFreeProxy.emit(ResultData.Companion.error(e))
            }
        }
    }

    fun getAllPremiumProxy(context: Context, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getCountries(type)
                if (response.isSuccessful) {
                    response.body()?.let { countriesResponse ->
                        val listProxies = countriesResponse.countries.map { country ->
                            ProxyGroupUI(
                                country = country, active = country == BaseAppConfig.proxyCountry
                            )
                        }
                        allPremiumProxy.emit(ResultData.Companion.success(listProxies))
                    }
                }
            } catch (e: Exception) {
                // Handle error case
                allPremiumProxy.emit(ResultData.Companion.error(e))
            }
        }
    }

    val activeProxy = MutableStateFlow<ResultData<ProxyUI?>>(ResultData.Companion.standby())

    fun getActiveProxy() {
        viewModelScope.launch(Dispatchers.IO) {
            proxyUseCase.getActiveProxy().collect { activeProxy.value = it }
        }
    }

    fun loadFirebase(context: Context) {
        viewModelScope.launch(
            Dispatchers.IO
        ) {

        }

    }

    suspend fun setActiveProxy(item: ProxyGroupUI?, deviceId: String?, type: String) {
        if (item != null && deviceId != null) {
            BaseAppConfig.proxyCountry = item.country

            // Call API to assign proxy
            val request = ProxyRequest(user_id = deviceId, country = item.country, type = type)
            val response = apiService.assignProxy(request)

            if (response.isSuccessful) {
                val assignResponse = response.body()
                if (assignResponse != null && assignResponse.proxy != null) {
                    BaseAppConfig.proxy = assignResponse.proxy
                    // Parse the proxy string (expected format: "ip:port:user:pass:type")
//                    val proxyParts = assignResponse.proxy.split(":")
//                    if (proxyParts.size == 5) {
//                        // Update proxy configuration from API response
//                        BaseAppConfig.apply {
//                            proxyHost = proxyParts[1]
//                            proxyPort = proxyParts[2]
//                            proxyUser = proxyParts[3]
//                            proxyPass = proxyParts[4]
//                            proxyType = proxyParts[0]
//                        }
//                    } else {
//                        throw Exception("Invalid proxy string format")
//                    }
                } else {
                    throw Exception(assignResponse?.error ?: "Empty proxy response")
                }
            } else {
                throw Exception("Failed to assign proxy: ${response.code()}")
            }
        } else {
            // Reset proxy when item is null
//            BaseAppConfig.apply {
//                proxyHost = ""
//                proxyPort = ""
//                proxyUser = ""
//                proxyPass = ""
//                proxyType = ""
//                proxyCountry = ""
//            }
            BaseAppConfig.proxy = ""
            BaseAppConfig.proxyCountry = ""

            // Notify API about disconnection if we have a device ID
            if (deviceId != null) {
                apiService.disconnect(DisconnectRequest(user_id = deviceId))
            }
        }
    }

    fun startProxy(context: Context?, allowApp: List<String>?) {
        LocalVpnService.startProxy(context!!, allowApp)
        proxyUpdate.setProxyUpdate(this)
    }

    fun stopProxy(context: Context?) {
        proxyUpdate.setProxyUpdate(this)
        LocalVpnService.stopProxy(context!!)
    }

    override fun updateUI(status: String) {
        isConnected.postValue(status)
    }
}