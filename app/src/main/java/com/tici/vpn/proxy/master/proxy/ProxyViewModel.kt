package com.tici.vpn.proxy.master.proxy

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.common.baseui.BaseAppConfig
import com.common.baseui.ResultData
import com.tici.vpn.proxy.master.api.ApiService
import com.tici.vpn.proxy.master.api.DisconnectRequest
import com.tici.vpn.proxy.master.api.ProxyRequest
import com.tici.vpn.proxy.master.api.Users
import com.tici.vpn.proxy.master.network.LocalVpnService
import com.tici.vpn.proxy.master.network.LocalVpnService.Companion.ACTION_START
import com.tici.vpn.proxy.master.network.LocalVpnService.Companion.ACTION_STOP
import com.tici.vpn.proxy.master.network.ProxySpeedTest
import com.tici.vpn.proxy.master.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
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

    val currentProxy = MutableLiveData<ProxySpeedTest.ProxyConfig?>()

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
                e.printStackTrace()
                allFreeProxy.emit(ResultData.Companion.error(e))
            }
        }
    }

    fun getAllPremiumProxy(context: Context, type: String) {
        Log.i("TestRelease", "getAllPremiumProxy ${type}")
        viewModelScope.launch(Dispatchers.IO) {
            Log.i("TestRelease", "getAllPremiumProxy viewModelScope ${type}")
            try {
                Log.i("TestRelease", "call api ${type} ${Utils.BASE_URL}")
                val response = apiService.getCountries(type)
                Log.i("TestRelease", "response ${response.isSuccessful} ${response.body()}")
                if (response.isSuccessful) {
                    response.body()?.let { countriesResponse ->
                        val listProxies = countriesResponse.countries.map { country ->
                            Log.i("TestRelease", "response country ${country.toString()}")
                            Log.i("TestRelease", "response active ${country == BaseAppConfig.proxyCountry}")
                            ProxyGroupUI(
                                country = country, active = country == BaseAppConfig.proxyCountry
                            )
                        }
                        allPremiumProxy.emit(ResultData.Companion.success(listProxies))
                    }
                }
            } catch (e: Exception) {
                // Handle error case
                e.printStackTrace()
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

            val request = ProxyRequest(user_id = deviceId, country = item.country, type = type)
            val response = apiService.assignProxy(request)

            if (response.isSuccessful) {
                val assignResponse = response.body()
                if (assignResponse != null) {
                    BaseAppConfig.proxy = assignResponse.proxy
                } else {
                    throw Exception(assignResponse?.error ?: "Empty proxy response")
                }
            } else {
                throw Exception("Failed to assign proxy: ${response.code()}")
            }
        } else {
            BaseAppConfig.proxy = ""
            BaseAppConfig.proxyHost = ""
            BaseAppConfig.proxyCountry = ""

            // Notify API about disconnection if we have a device ID
            if (deviceId != null) {
                apiService.disconnect(DisconnectRequest(user_id = deviceId))
            }
        }
    }

    fun startProxy(deviceId: String?, context: Context?, allowApp: List<String>?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context!!.startForegroundService(Intent(context, LocalVpnService::class.java).apply {
                action = ACTION_START
                putStringArrayListExtra("allowApp", allowApp as ArrayList<String>?)
            })
        } else {
            context!!.startService(Intent(context, LocalVpnService::class.java).apply {
                action = ACTION_START
                putStringArrayListExtra("allowApp", allowApp as ArrayList<String>?)
            })
        }

        viewModelScope.launch(Dispatchers.IO) {
            if (deviceId != null) {
                apiService.connect(
                    Users(
                        user_id = deviceId,
                        ip_address = BaseAppConfig.proxyHost,
                        type = "connect"
                    )
                )
            }
        }

        proxyUpdate.setProxyUpdate(this)
    }

    fun stopProxy(deviceId: String?, context: Context?) {
        proxyUpdate.setProxyUpdate(this)
        Log.i("SuperVpn", "TestRelease stopProxy")
        context!!.startService(Intent(context, LocalVpnService::class.java).apply {
            action = ACTION_STOP
        })
        viewModelScope.launch(Dispatchers.IO) {
            if (deviceId != null) {
                apiService.connect(
                    Users(
                        user_id = deviceId,
                        ip_address = BaseAppConfig.proxyHost,
                        type = "disconnect"
                    )
                )
            }
        }
    }

    override fun updateUI(status: String) {
        isConnected.postValue(status)
    }

    fun startProxyTest() {
        viewModelScope.launch(Dispatchers.IO) {
            engine.Engine.decodeString(BaseAppConfig.proxy).split(":").let { parts ->
                if (parts.size >= 2) {
                    currentProxy.postValue(
                        ProxySpeedTest.ProxyConfig(
                            host = parts[1],
                            port = parts[2].toInt(),
                            username = parts.getOrNull(3) ?: "",
                            password = parts.getOrNull(4) ?: "",
                            type = parts.getOrNull(0) ?: "http"
                        )
                    )
                } else {
                    currentProxy.postValue(null)
                }
            }
        }
    }
}