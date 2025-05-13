package com.akmobile.supervpn.proxy

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val proxyUseCase: ProxyInterface, private val proxyUpdate: ProxyConnection
) : ViewModel(), ISuperVpnProxyUpdate {
//    val allProxy = MutableStateFlow<ResultData<List<ProxyGroupUI>>>(ResultData.standby())

    val allProxy = MutableLiveData<List<ProxyGroupUI>>()

    val isConnected = MutableLiveData(proxyUpdate.vpnState)

    fun getAllProxy(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
//            proxyUseCase.getAllProxy().collect { allProxy.value = it }
            FirebaseApp.initializeApp(context)

            val listProxies = ArrayList<ProxyGroupUI>()

            val db = FirebaseFirestore.getInstance()
            val result = db.collection("proxies").get().await()

            val countrySet = mutableSetOf<String>();
            for (document in result) {
                countrySet.add(document.data["country"] as String)
            }

            for (country in countrySet) {
                val proxyCountry =
                    ProxyGroupUI(country = country, active = country == BaseAppConfig.proxyCountry)
                listProxies.add(proxyCountry)
            }

            allProxy.postValue(listProxies)

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

    suspend fun setActiveProxy(item: ProxyGroupUI?, deviceId: String) {
        if (item != null) {
            var result = FirebaseFirestore.getInstance().collection("proxies")
                .whereArrayContains("devices", deviceId).get().await()
            var owned = false
            for (proxy in result) {
                if (proxy.get("country") != item.country || owned) {
                    val devices = result.documents[0].data?.get("devices") as List<*>
                    FirebaseFirestore.getInstance().collection("proxies").document(proxy.id)
                        .update(
                            mutableMapOf(
                                "used_count" to (proxy.data?.get("used_count") as Long - 1),
                                "devices" to devices.minus(deviceId)
                            )
                        ).await()
                } else {
                    owned = true
                    BaseAppConfig.proxyHost = proxy.get("host") as String
                    BaseAppConfig.proxyPort = proxy.get("port") as String
                    BaseAppConfig.proxyUser = proxy.get("user") as String
                    BaseAppConfig.proxyPass = proxy.get("pass") as String
                    BaseAppConfig.proxyType = proxy.get("type") as String
                }
            }

            if (!owned) {
                result = FirebaseFirestore.getInstance().collection("proxies")
                    .orderBy("used_count", Query.Direction.ASCENDING).limit(1).get().await()
                val proxy = result.documents[0]
                BaseAppConfig.proxyHost = proxy.get("host") as String
                BaseAppConfig.proxyPort = proxy.get("port") as String
                BaseAppConfig.proxyUser = proxy.get("user") as String
                BaseAppConfig.proxyPass = proxy.get("pass") as String
                BaseAppConfig.proxyType = proxy.get("type") as String

                val devices = proxy.data?.get("devices") as List<*>

                FirebaseFirestore.getInstance().collection("proxies").document(proxy.id).update(
                    mutableMapOf(
                        "used_count" to (proxy.data?.get("used_count") as Long + 1),
                        "devices" to devices.plus(
                            deviceId
                        )
                    )
                ).await()
            }
            BaseAppConfig.proxyCountry = item.country
        } else {
            var result = FirebaseFirestore.getInstance().collection("proxies")
                .whereArrayContains("devices", deviceId).get().await()
            for (proxy in result) {
                val devices = result.documents[0].data?.get("devices") as List<*>
                FirebaseFirestore.getInstance().collection("proxies").document(proxy.id)
                    .update(
                        mutableMapOf(
                            "used_count" to (proxy.data?.get("used_count") as Long - 1),
                            "devices" to devices.minus(deviceId)
                        )
                    ).await()
            }
            BaseAppConfig.proxyHost = ""
            BaseAppConfig.proxyPort = ""
            BaseAppConfig.proxyUser = ""
            BaseAppConfig.proxyPass = ""
            BaseAppConfig.proxyType = ""
            BaseAppConfig.proxyCountry = ""
        }
//        viewModelScope.launch(Dispatchers.IO) {
//            proxyUseCase.setActiveProxy(id)
//        }
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