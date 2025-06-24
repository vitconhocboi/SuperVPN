package com.tici.vpn.proxy.master.settings.dns

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.common.baseui.BaseAppConfig
import com.tici.vpn.proxy.master.api.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DNSViewModel @Inject constructor(private val apiService: ApiService) : ViewModel() {

    val allDNS = MutableLiveData<List<DnsUI>>()

    val isLoading = MutableLiveData<Boolean>()

    fun getAllProxy(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.postValue(true)
            val response = apiService.getDns()
            if (response.isSuccessful) {
                response.body()?.let { dnsResponse ->
                    val listDNS = dnsResponse.dns.map { dns ->
                        DnsUI(
                            id = dns.ip_address,
                            name = dns.name,
                            server = dns.ip_address,
                            icon = "",
                            active = (BaseAppConfig.dnsServer.contains(dns.ip_address))
                        )
                    }
                    allDNS.postValue(listDNS)
                }
            }
            isLoading.postValue(false)
        }
    }

    fun saveDnsSetting(dns : ArrayList<DnsUI?>) {
        if (dns.isNotEmpty()) {
            val savedItems = dns.filter { it?.active == true }.map { it?.server }
            BaseAppConfig.dnsServer = savedItems.joinToString(", ")
        } else {
            BaseAppConfig.dnsServer = ""
        }
    }
}