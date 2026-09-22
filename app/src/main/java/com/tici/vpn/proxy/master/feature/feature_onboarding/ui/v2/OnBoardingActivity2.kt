package com.tici.vpn.proxy.master.feature.feature_onboarding.ui.v2

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.core.baseui.BaseCoreApplication
import com.core.analytics.AnalyticsEvent
import com.core.baseui.BaseActivity
import com.core.baseui.ext.collectFlowOn
import com.core.utilities.getStatusBarHeight
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

    val itemsOnboarding by lazy {
        OnBoardingItem.fromIntroData(remoteConfigRepository.getAppConfig().introDataV2)
    }

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
            viewPager.adapter = adapter
            viewPager.isUserInputEnabled = false
            viewPager.offscreenPageLimit = adapter.itemCount
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
                    if (BaseCoreApplication.isFirstSaveLanguage) {
                        BaseCoreApplication.isFirstSaveLanguage = false
                        analyticsManager.logEvent(AnalyticsEvent.EVENT_ACTION_PASS_INTRO)
                    }
                    openMain()
                }
            }
        }

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