package com.highsecure.vpn.proxy.master.settings.dns

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.common.baseui.BaseAppConfig
import com.common.baseui.ResultData
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.highsecure.vpn.proxy.master.api.ApiService
import com.highsecure.vpn.proxy.master.proxy.ProxyGroupUI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.collections.iterator

@HiltViewModel
class DNSViewModel @Inject constructor(private val apiService: ApiService) : ViewModel() {
    val allDNS = MutableLiveData<List<DnsUI>>()

    fun getAllProxy(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = apiService.getDns()
            if (response.isSuccessful) {
                response.body()?.let { dnsReponse ->
                    val listDNS = dnsReponse.dns.map { dns ->
                        DnsUI(
                            id = dns.ip_address,
                            name = dns.name,
                            server = dns.ip_address,
                            icon = "",
                            active = dns.ip_address == BaseAppConfig.dnsServer
                        )
                    }
                    allDNS.postValue(listDNS)
                }
            }
        }
    }
}