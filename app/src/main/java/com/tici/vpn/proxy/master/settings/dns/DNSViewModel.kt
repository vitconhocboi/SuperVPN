package com.tici.vpn.proxy.master.settings.dns

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.common.baseui.BaseAppConfig
import com.common.baseui.ResultData
import com.tici.vpn.proxy.master.api.ApiService
import com.tici.vpn.proxy.master.api.CountriesResponse
import com.tici.vpn.proxy.master.api.DnsResponse
import com.tici.vpn.proxy.master.proxy.ProxyGroupUI
import com.tici.vpn.proxy.master.utils.toast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.net.ssl.SSLHandshakeException

@HiltViewModel
class DNSViewModel @Inject constructor(private val apiService: ApiService) : ViewModel() {

    val allDNS = MutableLiveData<List<DnsUI>>()

    val isLoading = MutableLiveData<Boolean>()

    val errorMessage = MutableLiveData<String>()

    fun getAllProxy(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.postValue(true)
//            val response = apiService.getDns()
//            if (response.isSuccessful) {
//                response.body()?.let { dnsResponse ->
//                    val listDNS = dnsResponse.dns.map { dns ->
//                        DnsUI(
//                            id = dns.ip_address,
//                            name = dns.name,
//                            server = dns.ip_address,
//                            icon = "",
//                            active = (BaseAppConfig.dnsServer.contains(dns.ip_address))
//                        )
//                    }
//                    allDNS.postValue(listDNS)
//                }
//            }

            val result = getDnsSafe()
            result
                .onSuccess { dnsResponse ->
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
                    isLoading.postValue(false)
                }
                .onFailure { error ->
                    isLoading.postValue(false)
                    errorMessage.postValue("error_internet")
                }
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

    private suspend fun getDnsSafe(): Result<DnsResponse> {
        return try {
            val response = apiService.getDns()
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
}