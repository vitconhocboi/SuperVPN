package com.highsecure.vpn.proxy.master.splash

import android.annotation.SuppressLint
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.common.baseui.BaseAppConfig
import com.google.android.gms.ads.MobileAds
import com.highsecure.vpn.proxy.master.SuperVpnApplication
import com.highsecure.vpn.proxy.master.base.ProductActivity
import com.highsecure.vpn.proxy.master.databinding.ActivitySplashBinding
import com.highsecure.vpn.proxy.master.extension.launchIO
import com.highsecure.vpn.proxy.master.extension.launchMain
import com.highsecure.vpn.proxy.master.gdpr.GoogleMobileAdsConsentManager
import com.highsecure.vpn.proxy.master.language.LangType
import com.highsecure.vpn.proxy.master.remoteconfig.AdPlacementId
import com.highsecure.vpn.proxy.master.remoteconfig.FirebaseConfigManager
import com.highsecure.vpn.proxy.master.utils.Navigator
import com.simple.libads.AdNetworkType
import com.simple.libads.AdType
import com.simple.libads.NetworkUtils
import com.simple.libads.config.OpenConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@SuppressLint("CustomSplashScreen")
class SplashActivity : ProductActivity<ActivitySplashBinding>() {

    var adConfig: FirebaseConfigManager = FirebaseConfigManager.get()

    override fun bindingProvider(inflater: LayoutInflater): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(inflater)
    }

    private fun simulateProgress() {
        lifecycleScope.launch {
            var progress = 0
            while (progress < 100) {
                delay(20)  // Wait for 200ms
                progress += 5
                binding.customProgressBar.progress = progress
            }
            nextScreen()
        }
    }

    private fun checkLanguage() {
        val local = try {
            Resources.getSystem().configuration.locales[0]
        } catch (ex: Exception) {
            Resources.getSystem().configuration.locale
        }

        val locale = LangType.entries
            .firstOrNull { it.langCode.isNotEmpty() && local.language.contains(it.langCode) }
        if (BaseAppConfig.languageCode.isEmpty() && locale != null) {
            BaseAppConfig.languageCode = locale.langCode
        }
    }

    private fun nextScreen() {
        if (BaseAppConfig.firstTimeSetup) {
            Navigator.startLanguageActivity(this, fromSetting = false)
        } else {
            Navigator.startMainActivity(this)
        }
        finish()
    }

    override fun initView() {
        lifecycleScope.launch {
            launch { checkLanguage() }
            launch { if (!BaseAppConfig.isSub) initAdmob() }
            simulateProgress()
        }
        binding.adsNotice.visibility = if (!BaseAppConfig.isSub) View.VISIBLE else View.GONE
    }

    private fun initAdmob() {
        NetworkUtils.hasInternetAccessCheck(doTask = {
            val googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(this)
            googleMobileAdsConsentManager.gatherConsent(
                this,
                onCanShowAds = {
                    initAdmobSdk {
                    }
                },
                onDisableAds = {
                    nextScreen()
                }, timeout = 500
            )
            if (googleMobileAdsConsentManager.canRequestAds) {
                initAdmobSdk {
                }
            }
        }, doException = {
            initAdmobSdk {}
            nextScreen()
        })
    }

    private fun initAdmobSdk(onInit: () -> Unit) {
        launchIO {
            val testDeviceIds = listOf("FF2B80EE1C5DCA7A146F5303252D9DB3")
            val configuration = MobileAds.getRequestConfiguration()
                .toBuilder()
                .setTestDeviceIds(testDeviceIds)
                .build()
            MobileAds.setRequestConfiguration(configuration)
            MobileAds.initialize(this@SplashActivity) {
                launchMain {
                    adConfig.adOpenAds.map {
                        OpenConfig(
                            adnetwork = AdNetworkType.ADMOB,
                            adid = it.adId,
                            placementId = it.placementId,
                            ad_type = AdType.OPEN,
                            priority = 1
                        )
                    }.filter { it.placementId == AdPlacementId.OPEN_RESUME }.forEach {
                        SuperVpnApplication.instance.setupAds(it)
                    }
                    onInit()
                }
            }
        }

    }
}