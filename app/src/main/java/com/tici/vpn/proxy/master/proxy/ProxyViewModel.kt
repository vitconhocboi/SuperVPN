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
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport.Session.User
import com.tici.vpn.proxy.master.api.ApiService
import com.tici.vpn.proxy.master.api.AssignProxyResponse
import com.tici.vpn.proxy.master.api.CountriesResponse
import com.tici.vpn.proxy.master.api.CountriesResponseV2
import com.tici.vpn.proxy.master.api.DisconnectRequest
import com.tici.vpn.proxy.master.api.DisconnectResponse
import com.tici.vpn.proxy.master.api.FreeCountryResponse
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
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.net.ssl.SSLHandshakeException

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

    val freeProxy = MutableLiveData<ProxyGroupUI>()

    val isError = MutableLiveData(false)

    fun getAllFreeProxy(context: Context, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
//                val response = apiService.getCountries(type)
//                if (response.isSuccessful) {
//                    response.body()?.let { countriesResponse ->
//                        val listProxies = countriesResponse.countries.map { country ->
//                            ProxyGroupUI(
//                                country = country, active = country == BaseAppConfig.proxyCountry
//                            )
//                        }
//                        allFreeProxy.emit(ResultData.Companion.success(listProxies))
//                    }
//                }

//                val result = getCountriesSafe(type)
                val result = getCountriesV2Safe()
                result
                    .onSuccess { countriesResponse ->
                        val listProxies = countriesResponse.countries.map { country ->
                            ProxyGroupUI(
                                country = country.name,
                                active = country.name == BaseAppConfig.proxyCountry,
//                                isQuickAccess = country.is_quick_access == 1,
                                type = country.type
                            )
                        }
                        allFreeProxy.emit(ResultData.Companion.success(listProxies))
                    }
                    .onFailure { error ->
//                        throw Exception("Failed to get countries: $error")
                        Timber.d("isError getCountriesV2Safe $isError")
                        isError.postValue(true)
                    }
            } catch (e: Exception) {
                // Handle error case
                e.printStackTrace()
                allFreeProxy.emit(ResultData.Companion.error(e))
            }
        }
    }

    fun getAllPremiumProxy(context: Context, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Timber.d("load countries start")
//                val response = apiService.getCountries(type)
//                if (response.isSuccessful) {
//                    response.body()?.let { countriesResponse ->
//                        val listProxies = countriesResponse.countries.map { country ->
//                            ProxyGroupUI(
//                                country = country, active = country == BaseAppConfig.proxyCountry
//                            )
//                        }
//                        allPremiumProxy.emit(ResultData.Companion.success(listProxies))
//                    }
//                }

                val result = getCountriesV2Safe()
                result
                    .onSuccess { countriesResponse ->
                        Timber.d("load countries success size ${countriesResponse.countries.size}")
                        val listProxies = countriesResponse.countries.map { country ->
                            ProxyGroupUI(
                                country = country.name,
                                active = country.name == BaseAppConfig.proxyCountry,
//                                isQuickAccess = country.is_quick_access == 1,
                                type = country.type
                            )
                        }
                        Timber.d("load countries success size emit ${countriesResponse.countries.size}")
                        allPremiumProxy.emit(ResultData.Companion.success(listProxies))
                    }
                    .onFailure { error ->
//                        throw Exception("Failed to get countries: $error")
                        Timber.d("isError getCountriesV2Safe $isError")
                        isError.postValue(true)
                    }

            } catch (e: Exception) {
                // Handle error case
                e.printStackTrace()
                allPremiumProxy.emit(ResultData.Companion.error(e))
            }
        }
    }

    fun getRandomFreeProxy() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = getFreeCountrySafe()
                result
                    .onSuccess { freeCountry ->
                        with (freeCountry) {
                            freeProxy.postValue(ProxyGroupUI(
                                country = country,
                                active = country == BaseAppConfig.proxyCountry,
                                type = type
                            ))
                        }
                    }
                    .onFailure { error ->
//                        throw Exception("Failed to get countries: $error")
                        Timber.d("isError getCountriesV2Safe $isError")
                        isError.postValue(true)
                    }

            } catch (e: Exception) {
                // Handle error case
                e.printStackTrace()
                allPremiumProxy.emit(ResultData.Companion.error(e))
            }
        }
    }

    suspend fun getCountriesSafe(type: String): Result<CountriesResponse> {
        return try {
            val response = apiService.getCountries(type)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response from server"))
                }
            } else {
                Result.failure(Exception("Server error: ${response.code()}"))
            }
        } catch (e: SSLHandshakeException) {
            Result.failure(Exception("SSL Handshake failed"))
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("Connection timed out. Please try again later."))
        } catch (e: UnknownHostException) {
            Result.failure(Exception("No internet connection or DNS resolution failed"))
        } catch (e: IOException) {
            Result.failure(Exception("Network I/O error occurred"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCountriesV2Safe(): Result<CountriesResponseV2> {
        return try {
            val response = apiService.getCountriesV2()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response from server"))
                }
            } else {
                Result.failure(Exception("Server error: ${response.code()}"))
            }
        } catch (e: SSLHandshakeException) {
            Result.failure(Exception("SSL Handshake failed"))
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("Connection timed out. Please try again later."))
        } catch (e: UnknownHostException) {
            Result.failure(Exception("No internet connection or DNS resolution failed"))
        } catch (e: IOException) {
            Result.failure(Exception("Network I/O error occurred"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFreeCountrySafe(): Result<FreeCountryResponse> {
        return try {
            val response = apiService.getFreeCountry()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response from server"))
                }
            } else {
                Result.failure(Exception("Server error: ${response.code()}"))
            }
        } catch (e: SSLHandshakeException) {
            Result.failure(Exception("SSL Handshake failed"))
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("Connection timed out. Please try again later."))
        } catch (e: UnknownHostException) {
            Result.failure(Exception("No internet connection or DNS resolution failed"))
        } catch (e: IOException) {
            Result.failure(Exception("Network I/O error occurred"))
        } catch (e: Exception) {
            Result.failure(e)
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
        Timber.d("click country item setActiveProxy")
        if (item != null && deviceId != null) {
            Timber.d("click country item setActiveProxy assign ${item.country}")
            BaseAppConfig.proxyCountry = item.country

            val request = ProxyRequest(user_id = deviceId, country = item.country, type = type)
//            val response = apiService.assignProxy(request)
//
//            if (response.isSuccessful) {
//                val assignResponse = response.body()
//                if (assignResponse != null) {
//                    BaseAppConfig.proxy = assignResponse.proxy
//                } else {
//                    throw Exception(assignResponse?.error ?: "Empty proxy response")
//                }
//            } else {
//                throw Exception("Failed to assign proxy: ${response.code()}")
//            }

            val result = assignProxySafe(request)
            result
                .onSuccess { assignResponse ->
                    BaseAppConfig.proxy = assignResponse.proxy
                    addLastUsedVpn(item.country)
                }
                .onFailure { _ ->
//                    throw Exception("Failed to assign proxy: $error")
                    Timber.d("isError assignProxySafe $isError")
                    isError.postValue(true)
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
//                apiService.connect(
//                    Users(
//                        user_id = deviceId,
//                        ip_address = BaseAppConfig.proxyHost,
//                        type = "connect"
//                    )
//                )

                val result = connectSafe(
                    Users(
                        user_id = deviceId,
                        ip_address = BaseAppConfig.proxyHost,
                        type = "connect"
                    )
                )
                result
                    .onSuccess { _ ->

                    }
                    .onFailure { error ->
//                        throw Exception("Failed to assign proxy: $error")
                        Timber.d("isError connectSafe $isError")
                        isError.postValue(true)
                    }

            }
        }

        proxyUpdate.setProxyUpdate(this)
    }

    fun stopProxy(deviceId: String?, context: Context?) {
        proxyUpdate.setProxyUpdate(this)
//        Log.i("SuperVpn", "TestRelease stopProxy")
        context!!.startService(Intent(context, LocalVpnService::class.java).apply {
            action = ACTION_STOP
        })
        viewModelScope.launch(Dispatchers.IO) {
            if (deviceId != null) {
//                apiService.connect(
//                    Users(
//                        user_id = deviceId,
//                        ip_address = BaseAppConfig.proxyHost,
//                        type = "disconnect"
//                    )
//                )

                val result = connectSafe(
                    Users(
                        user_id = deviceId,
                        ip_address = BaseAppConfig.proxyHost,
                        type = "disconnect"
                    )
                )
                result
                    .onSuccess { _ ->

                    }
                    .onFailure { error ->
//                        throw Exception("Failed to assign proxy: $error")
                        Timber.d("isError connectSafe stopProxy $isError")
                        isError.postValue(true)
                    }
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

    suspend fun assignProxySafe(request: ProxyRequest): Result<AssignProxyResponse> {
        return try {
            val response = apiService.assignProxy(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response from server"))
                }
            } else {
                Result.failure(Exception("Server error: ${response.code()}"))
            }
        } catch (e: SSLHandshakeException) {
            Result.failure(Exception("SSL Handshake failed"))
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("Connection timed out. Please try again later."))
        } catch (e: UnknownHostException) {
            Result.failure(Exception("No internet connection or DNS resolution failed"))
        } catch (e: IOException) {
            Result.failure(Exception("Network I/O error occurred"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun connectSafe(user: Users): Result<DisconnectResponse> {
        return try {
            val response = apiService.connect(user)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response from server"))
                }
            } else {
                Result.failure(Exception("Server error: ${response.code()}"))
            }
        } catch (e: SSLHandshakeException) {
            Result.failure(Exception("SSL Handshake failed"))
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("Connection timed out. Please try again later."))
        } catch (e: UnknownHostException) {
            Result.failure(Exception("No internet connection or DNS resolution failed"))
        } catch (e: IOException) {
            Result.failure(Exception("Network I/O error occurred"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun addLastUsedVpn(country: String) {
        val current = BaseAppConfig.lastUsedVpn
        val items = current.split("|").filter { it.isNotEmpty() }.toMutableList()

        // maintain uniqueness and order
        items.remove(country)
        items.add(0, country)

        if (items.size > 3) {
            items.subList(3, items.size).clear()
        }

        BaseAppConfig.lastUsedVpn = items.joinToString("|")
    }

    fun getLastUsedVpn(): List<String> {
        val current = BaseAppConfig.lastUsedVpn
        return current.split("|").filter { it.isNotEmpty() }
    }
}