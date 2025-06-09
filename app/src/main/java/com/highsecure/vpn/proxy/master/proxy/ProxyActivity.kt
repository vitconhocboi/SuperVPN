package com.highsecure.vpn.proxy.master.proxy

import android.view.LayoutInflater
import android.view.View
import com.common.baseui.BaseAppConfig
import com.highsecure.vpn.proxy.master.R
import com.highsecure.vpn.proxy.master.base.ProductActivity
import com.highsecure.vpn.proxy.master.databinding.ActivityProxyBinding
import com.highsecure.vpn.proxy.master.main.MainActivity
import com.highsecure.vpn.proxy.master.proxy.fragments.TabProxyFragment
import com.highsecure.vpn.proxy.master.remoteconfig.AdPlacementId
import com.highsecure.vpn.proxy.master.remoteconfig.FirebaseConfigManager
import com.simple.libads.AdNetworkType
import com.simple.libads.base.bannerads.BannerLoader
import com.simple.libads.config.InterConfig
import com.simple.libads.manager.BannerManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProxyActivity : ProductActivity<ActivityProxyBinding>() {

    var configAd: FirebaseConfigManager = FirebaseConfigManager.get()

    private var mBannerLoader: BannerLoader? = null

    override fun initView() {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
            TabProxyFragment()
        )
            .commit()

        if (isBuyApp()) {
            binding.frameBanner.visibility = View.GONE
        } else {
            configAd.adBanner.find { it.placementId == AdPlacementId.BANNER_HOME }?.let {
                mBannerLoader =
                    BannerManager.createLoader(adNetwork = it.adNetwork, bannerId = it.adId)
                mBannerLoader?.canRequest = true
                mBannerLoader?.loadAds(
                    context = this@ProxyActivity,
                    bannerType = it.adType,
                    parent = binding.frameBanner
                )
            }
        }
    }

    override fun bindingProvider(inflater: LayoutInflater): ActivityProxyBinding {
        return ActivityProxyBinding.inflate(inflater)
    }

    override fun interConfigs(): List<InterConfig>? {
        return configAd.adWithVideo.placementIds.map {
            InterConfig(
                adid = configAd.adWithVideo.adId,
                adnetwork = AdNetworkType.ADMOB,
                placement_id = it
            )
        }
    }
}