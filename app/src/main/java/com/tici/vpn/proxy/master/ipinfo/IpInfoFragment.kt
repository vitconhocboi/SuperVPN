package com.tici.vpn.proxy.master.ipinfo

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.common.baseui.extension.setOnClickNoDoubleClick
import com.tici.vpn.proxy.master.base.ProductFragment
import com.tici.vpn.proxy.master.databinding.FragmentIpInfoBinding
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
import timber.log.Timber
import java.net.Authenticator
import java.net.InetSocketAddress
import java.net.PasswordAuthentication
import java.net.Proxy
import java.util.concurrent.TimeUnit

class IpInfoFragment : ProductFragment<FragmentIpInfoBinding>() {
    companion object {
        const val URL_IP_INFO = "https://free.freeipapi.com/api/json"
    }

    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentIpInfoBinding {
        return FragmentIpInfoBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        super.initView()
        showLoading()
        val proxyConfig = arguments?.getSerializable("proxyConfig") as? ProxyConfig
        Timber.d("SupperVPN", "getIpInfo initView: $proxyConfig")
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
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
            builder.proxyAuthenticator { _, response ->
                val credential = Credentials.basic(
                    proxyConfig.username, proxyConfig.password
                )
                response.request.newBuilder().header("Proxy-Authorization", credential)
                    .build()
            }
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
        Timber.d("SuperVPN getIpInfo")
        val request = Request.Builder().url(URL_IP_INFO)
            .header("User-Agent", "AndroidApp/1.0")
            .header("Accept", "application/json").build()
        try {
            Timber.d("SuperVPN getIpInfo try")
            client.newCall(request).execute().use { response ->
                Timber.d("SuperVPN getIpInfo respnse ${response.isSuccessful}")
                if (response.isSuccessful) {
                    val body = response.body.string()
                    val json = JSONObject(body)
                    binding.apply {
                        tvIpAddress.text = json.getString("ipAddress")
                        tvNetworkProvider.text = json.getString("asnOrganization")
                        "${json.getString("regionName")}, ${json.getString("countryName")}".also {
                            tvLocation.text = it
                        }
                        tvPincode.text = json.getString("zipCode")
                        tvTimeZone.text = json.getJSONArray("timeZones").getString(0)
                    }
                } else {
                    Timber.d("SuperVPN getIpInfo error connection ${response.code} ${response.isSuccessful}")
                }
                hideLoading()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    requireContext(),
                    "Connection failed. Please check the internet/VPN connection.",
                    Toast.LENGTH_SHORT
                ).show()
                hideLoading()
//                Navigator.startMainActivity(requireActivity())
            }
        }
    }
}