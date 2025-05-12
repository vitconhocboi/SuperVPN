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
import com.common.baseui.BaseAppConfig
import com.common.baseui.ResultData
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

@HiltViewModel
class ProxyViewModel @Inject constructor(
    private val proxyUseCase: ProxyInterface,
    private val proxyUpdate: ProxyConnection
) : ViewModel(), ISuperVpnProxyUpdate {
//    val allProxy = MutableStateFlow<ResultData<List<ProxyGroupUI>>>(ResultData.standby())

    val allProxy = MutableLiveData<List<ProxyUI>>()

    val isConnected = MutableLiveData(proxyUpdate.vpnState)

    fun getAllProxy(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
//            proxyUseCase.getAllProxy().collect { allProxy.value = it }
            FirebaseApp.initializeApp(context)

            val listProxies = ArrayList<ProxyUI>()

            val db = FirebaseFirestore.getInstance()
            val result = db.collection("Vpn")
                .get()
                .await()

            for (document in result) {
                val proxyCountry = ProxyGroupUI(country = document.id)
                Timber.d("Firestore Document ID: ${document.id}")
//                val listIps = ArrayList<ProxyUI>()
                for ((field, value) in document.data) {
                    val proxyConfig = value.toString().split(":")
                    if (proxyConfig.size >= 5) {
                        val proxy = ProxyUI(
                            type = proxyConfig[0],
                            name = proxyConfig[1],
                            id = proxyConfig[1],
                            host = proxyConfig[1],
                            country = document.id,
                            port = proxyConfig[2],
                            username = proxyConfig[3],
                            password = proxyConfig[4],
                            active = false
                        )
                        if (proxy.host == BaseAppConfig.proxyHost) {
                            proxy.active = true
                        }
                        Timber.d("Firestore Field: $field = $value")
                        listProxies.add(proxy)
                    }
                }
//                proxyCountry.list = listIps
//                listProxies.add(proxyCountry)
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

    fun setActiveProxy(item: ProxyUI?) {
        if (item != null) {
            BaseAppConfig.proxyHost = item.host
            BaseAppConfig.proxyPort = item.port
            BaseAppConfig.proxyUser = item.username
            BaseAppConfig.proxyPass = item.password
            BaseAppConfig.proxyType = item.type
            BaseAppConfig.proxyCountry = item.country
        } else {
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