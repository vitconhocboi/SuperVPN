package com.akmobile.supervpn.proxy

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akmobile.supervpn.api.ApiService
import com.akmobile.supervpn.api.DisconnectRequest
import com.akmobile.supervpn.api.ProxyRequest
import com.akmobile.supervpn.network.LocalVpnService
import com.common.baseui.BaseAppConfig
import com.common.baseui.ResultData
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ProxyViewModel @Inject constructor(
    private val proxyUseCase: ProxyInterface,
    private val proxyUpdate: ProxyConnection,
    private val apiService: ApiService  // Add API service injection
) : ViewModel(), ISuperVpnProxyUpdate {
    val allProxy = MutableStateFlow<ResultData<List<ProxyGroupUI>>>(ResultData.standby())

    val isConnected = MutableLiveData(proxyUpdate.vpnState)

    fun getAllProxy(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getCountries()
                if (response.isSuccessful) {
                    response.body()?.let { countriesResponse ->
                        val listProxies = countriesResponse.countries.map { country ->
                            ProxyGroupUI(
                                country = country, active = country == BaseAppConfig.proxyCountry
                            )
                        }
                        allProxy.emit(ResultData.success(listProxies))
                    }
                }
            } catch (e: Exception) {
                // Handle error case
                allProxy.emit(ResultData.error(e))
            }
        }
    }

    val activeProxy = MutableStateFlow<ResultData<ProxyUI?>>(ResultData.standby())

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

    suspend fun setActiveProxy(item: ProxyGroupUI?, deviceId: String?) {
        if (item != null && deviceId != null) {
            BaseAppConfig.proxyCountry = item.country

            // Call API to assign proxy
            val request = ProxyRequest(user_id = deviceId, country = item.country)
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