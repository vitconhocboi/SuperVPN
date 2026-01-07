package com.tici.vpn.proxy.master.feature.feature_onboarding.ui.v2

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.core.ads.BaseAdmobApplication
import com.core.ads.domain.AdLoadBannerNativeUiResource
import com.core.analytics.AnalyticsEvent
import com.core.baseui.BaseActivity
import com.core.baseui.ext.collectFlowOn
import com.core.config.domain.data.AppConfig.Companion.DEFINE_INTRO_FULL_AD
import com.core.config.domain.data.AppConfig.Companion.DEFINE_INTRO_HAVE_ADS
import com.core.config.domain.data.AppConfig.Companion.DEFINE_INTRO_NO_ADS
import com.core.config.domain.data.CoreAdPlaceName
import com.core.config.domain.data.IAdPlaceName
import com.core.utilities.getStatusBarHeight
import com.core.utilities.gone
import com.core.utilities.visibleIf
import com.tici.vpn.proxy.master.databinding.CoreActivityOnboardingBinding
import com.tici.vpn.proxy.master.feature.feature_onboarding.ui.adapter.OnBoardingPagerAdapter2
import com.tici.vpn.proxy.master.feature.feature_onboarding.ui.model.OnBoardingItem
import com.tici.vpn.proxy.master.feature.feature_onboarding.ui.v1.OnBoardingEvent
import com.tici.vpn.proxy.master.feature.feature_onboarding.ui.v1.OnBoardingViewModel
import com.tici.vpn.proxy.master.main.MainActivity
import com.tici.vpn.proxy.master.required.shortcut.AppShortCut
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingActivity2 : BaseActivity<CoreActivityOnboardingBinding>() {
    override val isHideStatusBar: Boolean
        get() = true

    override val isSpaceStatusBar: Boolean
        get() = false

    override val isSpaceDisplayCutout: Boolean
        get() = false


    private val sharedViewModel: OnBoardingViewModel by viewModels()

    override fun bindingProvider(inflater: LayoutInflater): CoreActivityOnboardingBinding {
        return CoreActivityOnboardingBinding.inflate(inflater)
    }

    private val targetScreenFromShortCut by lazy {
        intent.extras?.getString(AppShortCut.KEY_SHORTCUT_TARGET_SCREEN, "")
    }


    private val introData by lazy {
        remoteConfigRepository.getAppConfig().introDataV2.takeIf { it.isNotEmpty() } ?: arrayListOf(
            DEFINE_INTRO_HAVE_ADS,
            DEFINE_INTRO_HAVE_ADS,
            DEFINE_INTRO_HAVE_ADS
        )
    }

    val itemsOnboarding = ArrayList<OnBoardingItem>()

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
//            if (getCurrentLanguageCode() != appPreferences.systemLanguageCode) {
//                val intent = LanguageActivity.intentStart(
//                    this@OnBoardingActivity,
//                    fromIntroduction = true
//                ).apply {
//                    flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
//                }
//                startActivity(intent)
//            }
        }
    }

    override fun initViews(savedInstanceState: Bundle?) {
        onBackPressedDispatcher.addCallback(
            this,
            onBackPressedCallback
        )
        itemsOnboarding.apply {
            var indexIntro = 0
            introData.forEachIndexed { index, defineIntro ->

                when (defineIntro) {
                    DEFINE_INTRO_HAVE_ADS -> {
                        add(
                            OnBoardingItem.Item(
                                position = indexIntro,
                                isShowAds = !purchasePreferences.isUserVip(),
                                isPageEnd = false
                            )
                        )
                        indexIntro++
                    }

                    DEFINE_INTRO_NO_ADS -> {
                        add(
                            OnBoardingItem.Item(
                                position = indexIntro,
                                isShowAds = false,
                                isPageEnd = false
                            )
                        )
                        indexIntro++
                    }

                    DEFINE_INTRO_FULL_AD -> {
                        if (!adsManager.isNotAbleToVisibleAdsToUser(CoreAdPlaceName.ANCHORED_FULL_ONBOARDING_v2)) {
                            add(
                                OnBoardingItem.FullNativeItem
                            )
                        }
                    }
                }
            }
            itemsOnboarding.lastOrNull { it is OnBoardingItem.Item }?.isPageEnd = true
        }

        super.initViews(savedInstanceState)
        val adapter = OnBoardingPagerAdapter2(
            supportFragmentManager,
            this.lifecycle,
            itemsOnboarding
        )

        val params = binding.layoutToolbar.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = getStatusBarHeight()
        binding.layoutToolbar.layoutParams = params


        binding.run {
            viewPager.isUserInputEnabled = false
            viewPager.adapter = adapter
            viewPager.offscreenPageLimit = adapter.itemCount
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    val itemOnBoarding = itemsOnboarding[position]
                    layoutAds.visibleIf(itemOnBoarding.isShowAds && !purchasePreferences.isUserVip())
                }
            })
        }
    }

    override fun onBannerNativeResult(adResource: AdLoadBannerNativeUiResource) {
        super.onBannerNativeResult(adResource)
        if (adResource.commonAdPlaceName == CoreAdPlaceName.ANCHORED_ONBOARDING_BOTTOM_v2) {
            when (adResource) {
                is AdLoadBannerNativeUiResource.Loading -> {
                    binding.layoutBannerNative.setAdSize(
                        adResource.adType,
                        adResource.bannerSize,
                        adResource.nativeTemplateSize
                    )
                    val isShowAds =
                        (itemsOnboarding[binding.viewPager.currentItem]).isShowAds
                    if (isShowAds) {
                        binding.layoutBannerNative.visibleIf(!purchasePreferences.isUserVip())
                    }
                }

                is AdLoadBannerNativeUiResource.AdFailed -> {
                    binding.layoutBannerNative.gone()
                }

                is AdLoadBannerNativeUiResource.BannerAdLoaded -> {
                    val isShowAds =
                        (itemsOnboarding[binding.viewPager.currentItem]).isShowAds
                    binding.layoutBannerNative.onAdLoaded(adResource.bannerAd)
                    if (isShowAds) {
                        binding.layoutBannerNative.visibleIf(!purchasePreferences.isUserVip())
                    }
                }

                is AdLoadBannerNativeUiResource.NativeAdLoaded -> {
                    val isShowAds = (itemsOnboarding[binding.viewPager.currentItem]).isShowAds
                    binding.layoutBannerNative.onAdLoaded(
                        adResource.nativeAd,
                        adResource.nativeAdPlace
                    )
                    if (!isShowAds) {
                        binding.layoutBannerNative.visibleIf(!purchasePreferences.isUserVip())
                    }
                }

                is AdLoadBannerNativeUiResource.AdNetworkError -> {
                    /*if(isHideNativeBannerWhenNetworkError) {
                        binding.layoutBannerNative.gone()
                    }*/
                }
            }
        }
    }

    override fun handleObservable() {
        super.handleObservable()

        collectFlowOn(sharedViewModel.navigateToFlow) { event ->
            when (event) {
                OnBoardingEvent.BackEvent -> {

                }

                OnBoardingEvent.NextEvent -> {
                    binding.viewPager.setCurrentItem(
                        binding.viewPager.currentItem + 1,
                        true
                    )
                }

                OnBoardingEvent.FinishStep -> {
                    if (BaseAdmobApplication.isFirstSaveLanguage) {
                        BaseAdmobApplication.isFirstSaveLanguage = false
                        analyticsManager.logEvent(AnalyticsEvent.EVENT_ACTION_PASS_INTRO)
                    }
                    showInterAd(
                        CoreAdPlaceName.ACTION_NEXT_IN_INTRODUCTION
                    ) {
                        openMain()
                    }
                }
            }
        }

    }

    override fun providerBannerNativeAdPlaceName(): List<IAdPlaceName> {
        return listOf(CoreAdPlaceName.ANCHORED_ONBOARDING_BOTTOM_v2)
    }

    override fun providerInterAdPlaceName(): List<IAdPlaceName> {
        return listOf(
            CoreAdPlaceName.ACTION_NEXT_IN_INTRODUCTION,
            CoreAdPlaceName.ACTION_SKIP_IN_INTRODUCTION
        )
    }

    override fun onDestroy() {
        adsManager.releaseBannerNative(CoreAdPlaceName.ANCHORED_FULL_ONBOARDING_v2)
        super.onDestroy()
    }

    private fun openMain() {
        val intent = Intent(this, MainActivity::class.java)
        val bundle = Bundle().apply {
            putString(AppShortCut.KEY_SHORTCUT_TARGET_SCREEN, targetScreenFromShortCut)
        }
        intent.putExtras(bundle)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        this.startActivity(intent)
    }
}