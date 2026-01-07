package com.tici.vpn.proxy.master.proxy

import android.os.Bundle
import android.view.LayoutInflater
import com.core.ads.domain.AdLoadBannerNativeUiResource
import com.core.baseui.BaseActivity
import com.core.config.domain.data.IAdPlaceName
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.databinding.ActivityProxyBinding
import com.tici.vpn.proxy.master.required.ads.AppAdPlaceName
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProxyActivity : BaseActivity<ActivityProxyBinding>() {

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun initViews(savedInstanceState: Bundle?) {
        supportFragmentManager.beginTransaction().replace(
            R.id.fragment_container,
//            PremiumProxyFragment()
            TabProxyFragment()
        )
            .commit()
    }

    override fun bindingProvider(inflater: LayoutInflater): ActivityProxyBinding {
        return ActivityProxyBinding.inflate(inflater)
    }

    override fun providerBannerNativeAdPlaceName(): List<IAdPlaceName> {
        return listOf(
            AppAdPlaceName.ANCHORED_BOTTOM_VPN_SEVERS
        )
    }

    override fun onBannerNativeResult(adResource: AdLoadBannerNativeUiResource) {
        binding.layoutBannerNative.processAdResource(
            adResource,
            AppAdPlaceName.ANCHORED_BOTTOM_VPN_SEVERS
        )
    }
}