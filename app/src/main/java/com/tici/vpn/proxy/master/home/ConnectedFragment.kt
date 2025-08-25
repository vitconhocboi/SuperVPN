package com.tici.vpn.proxy.master.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import com.common.baseui.BaseAppConfig
import com.common.baseui.extension.context
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.base.ProductFragment
import com.tici.vpn.proxy.master.databinding.FragmentConnectedBinding
import com.tici.vpn.proxy.master.network.ProxySpeedTest
import com.tici.vpn.proxy.master.utils.Navigator
import com.tici.vpn.proxy.master.utils.Utils

class ConnectedFragment : BackActionBarFragment, ProductFragment<FragmentConnectedBinding>() {
    private var currentProxy: ProxySpeedTest.ProxyConfig? = null
    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentConnectedBinding {
        return FragmentConnectedBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        super.initView()
        binding.apply {
            pnIpInfo.setOnClickListener {
                Navigator.startProxyInfoActivity(requireActivity(), currentProxy)
            }
            if (BaseAppConfig.proxy.isNotEmpty()) {
                ivFlag.setImageResource(Utils.getFlag(BaseAppConfig.proxyCountry))
                tvCountry.text = requireContext().safeGetString(BaseAppConfig.proxyCountry)
                val typeface = ResourcesCompat.getFont(context, R.font.inter_bold)
                tvCountry.typeface = typeface
                tvCountry.setTextColor(resources.getColor(R.color.language_item_text_color))
            } else {
                ivFlag.setImageResource(R.drawable.ic_earth)
                tvCountry.text = getString(R.string.ip_proxy)
                val typeface = ResourcesCompat.getFont(context, R.font.inter_normal)
                tvCountry.typeface = typeface
                tvCountry.setTextColor(resources.getColor(R.color.green_1))
            }
        }
    }

    fun Context.safeGetString(resourceName: String): String? {
        val resId = resources.getIdentifier(resourceName, "string", packageName)
        return if (resId != 0) getString(resId) else null
    }

    override fun getTitle(): String {
        return getString(R.string.connect_success)
    }

    fun setCurrentProxy(proxy: ProxySpeedTest.ProxyConfig?) {
        currentProxy = proxy
    }
}