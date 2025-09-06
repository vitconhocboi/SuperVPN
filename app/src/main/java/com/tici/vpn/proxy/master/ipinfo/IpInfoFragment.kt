package com.tici.vpn.proxy.master.ipinfo

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.common.baseui.extension.setOnClickNoDoubleClick
import com.tici.vpn.proxy.master.base.ProductFragment
import com.tici.vpn.proxy.master.databinding.FragmentIpInfoBinding
import com.tici.vpn.proxy.master.network.LocalVpnService
import com.tici.vpn.proxy.master.network.ProxySpeedTest.ProxyConfig
import com.tici.vpn.proxy.master.utils.Navigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.json.JSONObject
import java.net.Authenticator
import java.net.InetSocketAddress
import java.net.PasswordAuthentication
import java.net.Proxy
import java.util.concurrent.TimeUnit

class IpInfoFragment : ProductFragment<FragmentIpInfoBinding>() {
    companion object {
        const val URL_IPINFO = "https://free.freeipapi.com/api/json"
    }

    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentIpInfoBinding {
        return FragmentIpInfoBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        super.initView()
        showLoading()
        val proxyConfig =
            if (LocalVpnService.IsRunning) (arguments?.getSerializable("proxyConfig") as? ProxyConfig) else null
        Log.d("SUPERVPN", "initView: $proxyConfig")
        viewLifecycleOwner.lifecycleScope.launch {
            getIpInfo(buildOkHttpClient(proxyConfig))
        }

        binding.ivBack.setOnClickNoDoubleClick {
            Navigator.startMainActivity(requireActivity())
        }
    }

    private fun buildOkHttpClient(proxyConfig: ProxyConfig?): OkHttpClient {
        val builder = OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS).writeTimeout(15, TimeUnit.SECONDS)
        if (proxyConfig != null) {
            val proxy = Proxy(
                when (proxyConfig.type.uppercase()) {
                    "SOCKS5" -> Proxy.Type.SOCKS
                    "HTTP" -> Proxy.Type.HTTP
                    else -> throw IllegalArgumentException("Unsupported proxy type: ${proxyConfig.type}")
                }, InetSocketAddress(proxyConfig.host, proxyConfig.port)
            )
            builder.proxy(proxy)
        }
        if (proxyConfig?.type?.uppercase() == "HTTP") {
            builder.proxyAuthenticator(object : okhttp3.Authenticator {
                override fun authenticate(route: Route?, response: Response): Request? {
                    val credential = Credentials.basic(
                        proxyConfig.username, proxyConfig.password
                    )
                    return response.request.newBuilder().header("Proxy-Authorization", credential)
                        .build()
                }
            })
        } else if (proxyConfig?.type?.uppercase() == "SOCKS5") {
            Authenticator.setDefault(object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(
                        proxyConfig.username, proxyConfig.password.toCharArray()
                    )
                }
            })
        }
        return builder.build()
    }

    private suspend fun getIpInfo(client: OkHttpClient) = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(URL_IPINFO)
            .header("User-Agent", "AndroidApp/1.0")
            .header("Accept", "application/json").build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body.string()
                    val json = JSONObject(body)
                    Log.d("SupperVPN", "getIpInfo: $json")
                    binding.apply {
                        tvIpAddress.text = json.getString("ipAddress")
                        tvNetworkProvider.text = json.getString("asnOrganization")
                        "${json.getString("regionName")}, ${json.getString("countryName")}".also {
                            tvLocation.text = it
                        }
                        tvPincode.text = json.getString("zipCode")
                        tvTimeZone.text = json.getJSONArray("timeZones").getString(0)
                    }
                }
                hideLoading()
            }
        } catch (e: Exception) {
            Log.d("SupperVPN", "getIpInfo:", e)
            hideLoading()
        }
    }
}