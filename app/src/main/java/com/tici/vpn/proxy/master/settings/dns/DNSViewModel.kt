package com.tici.vpn.proxy.master.settings.dns

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.common.baseui.BaseAppConfig
import com.tici.vpn.proxy.master.api.ApiService
import com.tici.vpn.proxy.master.api.DnsResponse
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

            val defaultList = getDefaultDnsList()
            val result = getDnsSafe()
            result
                .onSuccess { dnsResponse ->
                    val remoteList = dnsResponse.dns.map { dns ->
                        DnsUI(
                            id = dns.ip_address,
                            name = dns.name,
                            server = dns.ip_address,
                            icon = "",
                            active = isDnsActive(dns.ip_address)
                        )
                    }
                    val combinedList = mergeDnsLists(defaultList, remoteList)
                    allDNS.postValue(combinedList)
                    isLoading.postValue(false)
                }
                .onFailure {
                    // Fallback to default free DNS list when network/endpoint is unavailable
                    allDNS.postValue(defaultList)
                    isLoading.postValue(false)
                }
        }
    }

    private fun getDefaultDnsList(): List<DnsUI> {
        val servers = listOf(
            DnsUI("google", "Google Public DNS", "8.8.8.8, 8.8.4.4", ""),
            DnsUI("cloudflare", "Cloudflare DNS", "1.1.1.1, 1.0.0.1", ""),
            DnsUI("quad9", "Quad9 DNS", "9.9.9.9, 149.112.112.112", ""),
            DnsUI("opendns", "OpenDNS", "208.67.222.222, 208.67.220.220", ""),
            DnsUI("adguard", "AdGuard DNS", "94.140.14.14, 94.140.15.15", ""),
            DnsUI("cleanbrowsing", "CleanBrowsing DNS", "185.228.168.9, 185.228.169.9", "")
        )
        return servers.map { dns ->
            dns.copy(active = isDnsActive(dns.server))
        }
    }

    private fun isDnsActive(server: String): Boolean {
        val currentDns = BaseAppConfig.dnsServer
        if (currentDns.isBlank()) return false
        val currentIps = currentDns.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val serverIps = server.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        return serverIps.any { currentIps.contains(it) }
    }

    private fun mergeDnsLists(defaultList: List<DnsUI>, remoteList: List<DnsUI>): List<DnsUI> {
        val defaultIps = defaultList.flatMap { it.server.split(",").map { s -> s.trim() } }.toSet()
        val uniqueRemote = remoteList.filter { remote ->
            remote.server.split(",").map { it.trim() }.none { defaultIps.contains(it) }
        }
        return defaultList + uniqueRemote
    }

    fun saveDnsSetting(dns: ArrayList<DnsUI?>) {
        if (dns.isNotEmpty()) {
            val savedItems = dns.filter { it?.active == true }.mapNotNull { it?.server }
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
            Result.failure(Exception("Connection timed out"))
        } catch (e: UnknownHostException) {
            Result.failure(Exception("No internet connection or DNS resolution failed"))
        } catch (e: IOException) {
            Result.failure(Exception("Network I/O error occurred"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
