package com.tici.vpn.proxy.master.feature.feature_splash.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.viewbinding.ViewBinding
import com.core.ads.domain.AdFullScreenUiResource
import com.core.ads.domain.AdOpenAdUiResource
import com.core.ads.domain.ConsentFormUiResource
import com.core.ads.model.PreventShowManyInterstitialAds
import com.core.analytics.AnalyticsEvent
import com.core.baseui.BillingViewModel
import com.core.baseui.countdown.JsgCountDownTimer
import com.core.baseui.ext.collectFlowOn
import com.core.config.data.FetchRemoteConfigState
import com.core.config.domain.data.AdType
import com.core.config.domain.data.AppConfig
import com.core.config.domain.data.CoreAdPlaceName
import com.core.config.domain.data.IAdPlaceName
import com.core.preference.SharedPrefs
import com.core.utilities.getCurrentLanguageCode
import com.core.utilities.hideNavigationBar
import com.core.utilities.manager.isNetworkConnected
import com.core.utilities.util.Timber
import com.tici.vpn.proxy.master.core.base_ui.CoreActivity
import com.tici.vpn.proxy.master.feature.feature_language.ui.LanguageActivity
import com.tici.vpn.proxy.master.feature.feature_onboarding.ui.OnBoardingActivity
import com.tici.vpn.proxy.master.feature.feature_uninstall.ui.UninstallActivityHost
import com.tici.vpn.proxy.master.main.MainActivity
import com.tici.vpn.proxy.master.required.ads.AppAdPlaceName
import com.tici.vpn.proxy.master.required.inapp.InAppBillingViewModel
import com.tici.vpn.proxy.master.required.shortcut.AppScreenType
import com.tici.vpn.proxy.master.required.shortcut.AppShortCut
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import java.util.concurrent.TimeUnit


private const val TAG = "BaseSplashActivity"

abstract class BaseSplashActivity<VB : ViewBinding> : CoreActivity<VB>() {


    private val viewModel by viewModels<SplashViewModel>()


    private val inAppBillingViewModel: BillingViewModel by viewModels<InAppBillingViewModel>()

    private var timeShowIntro by SharedPrefs.instance.preference(
        defaultValue = 0L,
        key = "timeShowIntro"
    )

    private var countDownTimer: JsgCountDownTimer? = null

    private val appOpenPlaceName by lazy {
        if (viewModel.isFirstOpenApp) {
            CoreAdPlaceName.APP_OPEN_FIRST_OPEN
        } else {
            CoreAdPlaceName.APP_OPEN
        }
    }

    private val interstitialPlaceName by lazy {
        if (viewModel.isFirstOpenApp) {
            CoreAdPlaceName.ACTION_OPEN_APP_FIRST_OPEN
        } else {
            CoreAdPlaceName.ACTION_OPEN_APP
        }
    }

    private val isEnableIntroductionScreen: Boolean by lazy {
        remoteConfigRepository.getAppConfig().isEnableIntroductionScreen
    }
    private val isEnableLanguageScreen: Boolean by lazy {
        remoteConfigRepository.getAppConfig().isEnableChangeLanguageScreen
    }
    private val isAlwaysShowIntroAndLanguageScreen: Boolean by lazy {
        if (timeShowIntro == 0L) {
            remoteConfigRepository.getAppConfig().isAlwaysShowIntroAndLanguageScreen
        } else {
            val subDate = subDate(System.currentTimeMillis(), timeShowIntro)
            if (subDate >= remoteConfigRepository.getAppConfig().intervalDayAlwaysShowIntroAndLanguage) {
                remoteConfigRepository.getAppConfig().isAlwaysShowIntroAndLanguageScreen
            } else {
                false
            }
        }
    }

    private fun subDate(currentTime: Long, previousTime: Long): Int {
        val calCurrent = Calendar.getInstance().apply { timeInMillis = currentTime }
        val calPrevious = Calendar.getInstance().apply { timeInMillis = previousTime }

        // Reset giờ, phút, giây, mili giây về 0 để chỉ so sánh ngày
        calCurrent.set(Calendar.HOUR_OF_DAY, 0)
        calCurrent.set(Calendar.MINUTE, 0)
        calCurrent.set(Calendar.SECOND, 0)
        calCurrent.set(Calendar.MILLISECOND, 0)

        calPrevious.set(Calendar.HOUR_OF_DAY, 0)
        calPrevious.set(Calendar.MINUTE, 0)
        calPrevious.set(Calendar.SECOND, 0)
        calPrevious.set(Calendar.MILLISECOND, 0)

        val diffMillis = calCurrent.timeInMillis - calPrevious.timeInMillis
        return TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()
    }

    private var openInternetConnectivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    /**Shortcut Data - Điều hướng màn hình theo shortcut*/
    private val targetScreenFromShortCut by lazy {
        intent.extras?.getString(AppShortCut.KEY_SHORTCUT_TARGET_SCREEN, "")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        onBackPressedDispatcher.addCallback(
            this,
            onBackPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                }
            }
        )

        appOpenAdManager.setupDefaultValue()
        hideNavigationBar()
        initView()
        initData()
    }

    abstract fun initData()
    abstract fun hideLoading()

    fun onDataReady() {
        val eventName = if (isNetworkConnected()) {
            AnalyticsEvent.NETWORK_AVAILABLE
        } else {
            AnalyticsEvent.NETWORK_NOT_AVAILABLE
        }
        analyticsManager.logEvent(eventName)

        if (isNetworkConnected()) {
            remoteConfigRepository.fetchAndActive()
        } else {
            showRequireTurnOnNetworkBottomSheetFragment()
        }
    }


    private fun initView() {
        when (targetScreenFromShortCut) {
            AppScreenType.Uninstall.screenName -> {
                if (getCurrentLanguageCode().isBlank()) {
                    analyticsManager.logEvent(AnalyticsEvent.EVENT_CLICK_SHORT_CUT_UNINSTALL_BEFORE_SET_LANGUAGE)
                } else {
                    analyticsManager.logEvent(AnalyticsEvent.EVENT_CLICK_SHORT_CUT_UNINSTALL)
                }
            }

            else -> {
                if (targetScreenFromShortCut != null) {
                    analyticsManager.logEvent(AnalyticsEvent.EVENT_CLICK_SHORT_CUT + targetScreenFromShortCut)
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideNavigationBar()
    }

    override fun providerInterAdPlaceName(): List<IAdPlaceName> {
        return mutableListOf<IAdPlaceName>().apply {
            if (getCurrentLanguageCode().isBlank() || isAlwaysShowIntroAndLanguageScreen) {
                add(CoreAdPlaceName.ACTION_NEXT_IN_INTRODUCTION)
                add(CoreAdPlaceName.ACTION_SKIP_IN_INTRODUCTION)
            }
        }
    }

    override fun providerRewardAdPlaceName(): List<IAdPlaceName> {
        return listOf(
            AppAdPlaceName.REWARDED_CONNECT_VPN
        )
    }

    override fun providerPreloadBannerNativeAdPlaceName(): List<IAdPlaceName> {
        val isLoadLanguage =
            getCurrentLanguageCode().isBlank() || isAlwaysShowIntroAndLanguageScreen
        return mutableListOf<IAdPlaceName>().apply {
            if (isLoadLanguage) {
                add(CoreAdPlaceName.ANCHORED_CHANGE_LANGUAGE_BOTTOM)
            }
            if (isEnableIntroductionScreen && isLoadLanguage) {
                add(CoreAdPlaceName.ANCHORED_ONBOARDING_BOTTOM)
                if (remoteConfigRepository.getAppConfig().introData.contains(AppConfig.DEFINE_INTRO_FULL_AD)) {
                    add(CoreAdPlaceName.ANCHORED_FULL_ONBOARDING)
                }
            }

            if (targetScreenFromShortCut == AppScreenType.Uninstall.screenName) {
                add(CoreAdPlaceName.ANCHORED_UNINSTALL_BOTTOM_STEP_1)
                add(CoreAdPlaceName.ANCHORED_UNINSTALL_BOTTOM_STEP_2)
            }

            add(AppAdPlaceName.ANCHORED_BOTTOM_HOME)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.isActivityResume = true
        if (viewModel.needHandleEventWhenResume) {
            viewModel.needHandleEventWhenResume = false
            showRequireTurnOnNetworkBottomSheetFragment()
        }

        if (countDownTimer?.isTimerPaused() == true) {
            countDownTimer?.resumeTimer()
        }
        if (countDownTimer == null && viewModel.isRequestEuConsentComplete) {
            startCountDownTimer()
        }
    }

    override fun handleObservable() {
        val ignoreSuper = true
        if (!ignoreSuper) {
            super.handleObservable()
        }
        collectFlowOn(remoteConfigRepository.fetchStateCompleteFlow) { fetchState ->
            Timber.e("fetchState: $fetchState")
            when (fetchState) {
                FetchRemoteConfigState.Loading -> {

                }

                is FetchRemoteConfigState.Complete -> {
                    AppShortCut.setUpShortCut(
                        this,
                        remoteConfigRepository.getAppConfig().isEnableAppShortCut,
                        remoteConfigRepository.getAppConfig().isEnableAppShortcutUninstall
                    )
                    PreventShowManyInterstitialAds.initIntervalTimeShowInterstitialMillis()
                    adsManager.startDisableAdCountDownTimer()
                    val isShowAd = when {
                        purchasePreferences.isUserVip() -> false
                        targetScreenFromShortCut.isNullOrBlank() -> true
                        else -> {
                            if (targetScreenFromShortCut == AppScreenType.Uninstall.screenName) {
                                remoteConfigRepository.getAppConfig().isEnableOpenAppAdsFromUninstallShortcut
                            } else {
                                remoteConfigRepository.getAppConfig().isEnableOpenAppAdsFromShortcut
                            }
                        }
                    }

                    if (isShowAd) {
                        if (remoteConfigRepository.getSplashScreenConfig().isLoadBeforeEuConsent) {
                            fetchSplashAds()
                        }
                        Timber.e("requestConsentInfoUpdate")
                        adsManager.requestConsentInfoUpdate(this, false)
                    } else {
                        handleWhenAdNotValidOrLoadFailed()
                        startCountDownTimer()
                    }
                }
            }
        }

        collectFlowOn(adsManager.requestConsentFlow) { uiResource ->
            when (uiResource) {
                ConsentFormUiResource.Loading -> {}

                ConsentFormUiResource.Showing -> {}

                ConsentFormUiResource.Complete -> {
                    Timber.e("ConsentFormUiResource.Complete")
                    preloadAds()
                    viewModel.isRequestEuConsentComplete = true
                    if (!remoteConfigRepository.getSplashScreenConfig().isLoadBeforeEuConsent) {
                        fetchSplashAds()
                    }
                    if (viewModel.isActivityResume) {
                        startCountDownTimer()
                    }
                }
            }
        }

        collectFlowOn(appOpenAdManager.adOpenAppFlow) { uiResource ->
            Timber.e("appOpenAdManager.adOpenAppFlow $uiResource")
            if (uiResource.rootAdPlaceName == appOpenPlaceName) {
                when (uiResource) {
                    is AdOpenAdUiResource.AdLoaded -> {
                        handleWhenAdLoaded()
                    }

                    is AdOpenAdUiResource.AdNotValidOrLoadFailed -> {
                        handleWhenAdNotValidOrLoadFailed()
                    }

                    is AdOpenAdUiResource.AdShowing -> {
                        handleWhenAdShowing()
                    }

                    is AdOpenAdUiResource.AdDismissed -> {
                        handleWhenAdDismissed()
                    }
                }
            }
        }

        collectFlowOn(adsManager.adFullScreenFlow) { uiResource ->
            Timber.e("appOpenAdManager.adFullScreenFlow $uiResource")
            if (uiResource.rootAdPlaceName == interstitialPlaceName) {
                when (uiResource) {
                    is AdFullScreenUiResource.AdLoaded -> {
                        handleWhenAdLoaded()
                    }

                    is AdFullScreenUiResource.AdNotValidOrLoadFailed -> {
                        handleWhenAdNotValidOrLoadFailed()
                    }

                    is AdFullScreenUiResource.AdSucceedToShow -> {
                        handleWhenAdShowing()
                    }

                    is AdFullScreenUiResource.AdDismissed -> {
                        handleWhenAdDismissed()
                    }

                    else -> {}
                }
            }
        }

        collectFlowOn(viewModel.showRequireTurnOnNetworkWhenRetryClickedFlow) {
            if (viewModel.isActivityResume) {
                showRequireTurnOnNetworkBottomSheetFragment()
            } else {
                viewModel.needHandleEventWhenResume = true
            }
        }
    }

    private fun fetchSplashAds() {
        Log.d(TAG, "fetchSplashAds: 0")
        if (viewModel.isFirstOpenApp) {
            if (remoteConfigRepository.getSplashScreenConfig().adTypeFirstOpen == AdType.AppOpen) {
                Log.d(TAG, "fetchSplashAds: 1")
                appOpenAdManager.fetchAd(this, appOpenPlaceName)
            } else {
                Log.d(TAG, "fetchSplashAds: 2")
                adsManager.loadFullscreenAd(this, interstitialPlaceName, isNeedUpdateAdPlace = true)
            }
        } else {
            if (remoteConfigRepository.getSplashScreenConfig().adType == AdType.AppOpen) {
                Log.d(TAG, "fetchSplashAds: 3")
                appOpenAdManager.fetchAd(this, appOpenPlaceName)
            } else {
                Log.d(TAG, "fetchSplashAds: 4")
                adsManager.loadFullscreenAd(this, interstitialPlaceName, isNeedUpdateAdPlace = true)
            }
        }
    }

    private fun handleWhenAdLoaded() {
        Log.d(TAG, "handleWhenAdLoaded: ")
        viewModel.handleWhenAdLoaded()
    }

    private fun handleWhenAdNotValidOrLoadFailed() {
        Log.d(TAG, "handleWhenAdNotValidOrLoadFailed: ")
        viewModel.handleWhenAdNotValidOrLoadFailed()
        checkAbleToNextScreen()
    }

    private fun handleWhenAdShowing() {
        Log.d(TAG, "handleWhenAdShowing: ")
        hideLoading()
        viewModel.handleWhenAdShowing()
    }

    private fun handleWhenAdDismissed() {
        Log.d(TAG, "handleWhenAdDismissed: ")
        viewModel.handleWhenAdDismissed()
        checkAbleToNextScreen()
    }

    override fun onPause() {
        super.onPause()
        viewModel.isActivityResume = false
        if (countDownTimer?.isTimerRunning() == true) {
            countDownTimer?.pauseTimer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appOpenAdManager.isFirstOpenApp = false
        coroutineContext.cancelChildren()
        stopCountDown()
    }

    private fun stopCountDown() {
        try {
            countDownTimer?.pauseTimer()
            countDownTimer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startCountDownTimer() {
        if (countDownTimer != null) {
            countDownTimer?.pauseTimer()
            countDownTimer = null
        }

        val minTimeWaitProgressBeforeShowAd =
            remoteConfigRepository.getSplashScreenConfig().minTimeWaitProgressBeforeShowAd * 1000L
        val timeMillisDelayBeforeShow =
            remoteConfigRepository.getAppOpenAdConfig().timeMillisDelayBeforeShow

        viewModel.maxProgress =
            remoteConfigRepository.getSplashScreenConfig().maxTimeToWaitAppOpenAd * 1000L

        Timber.e("startCountDownTimer ${viewModel.maxProgress}")

        countDownTimer = object : JsgCountDownTimer(viewModel.maxProgress, 100) {
            override fun onTimerTick(timeRemaining: Long) {
                viewModel.currentProgress = viewModel.maxProgress - timeRemaining
                Timber.e("startCountDownTimer ${viewModel.currentProgress}")
                if (viewModel.isAppOpenAdLoaded) {
                    Timber.e("isAppOpenAdLoaded ${viewModel.isAppOpenAdLoaded}")
                    if (viewModel.currentProgress >= minTimeWaitProgressBeforeShowAd) {
                        viewModel.isAppOpenAdLoaded = false
                        runBlocking {
                            delay(timeMillisDelayBeforeShow)
                            if (viewModel.isFirstOpenApp) {
                                if (remoteConfigRepository.getSplashScreenConfig().adTypeFirstOpen == AdType.AppOpen) {
                                    appOpenAdManager.showAdIfAvailable(
                                        this@BaseSplashActivity,
                                        appOpenPlaceName
                                    )
                                } else {
                                    adsManager.showAd(
                                        this@BaseSplashActivity,
                                        interstitialPlaceName
                                    )
                                }
                            } else {
                                if (remoteConfigRepository.getSplashScreenConfig().adType == AdType.AppOpen) {
                                    appOpenAdManager.showAdIfAvailable(
                                        this@BaseSplashActivity,
                                        appOpenPlaceName
                                    )
                                } else {
                                    adsManager.showAd(
                                        this@BaseSplashActivity,
                                        interstitialPlaceName
                                    )
                                }
                            }
                        }
                    }
                    return
                }
                checkAbleToNextScreen()
            }

            override fun onTimerFinish() {
                viewModel.isTimerComplete = true
                checkAbleToNextScreen()
            }
        }
        countDownTimer?.startTimer()
    }

    private fun checkAbleToNextScreen() {
        if (isFinishing || isDestroyed) return

        val nextScreen = {
            countDownTimer?.pauseTimer()
            appOpenAdManager.isFirstOpenApp = false
            val intent =
                when {
                    targetScreenFromShortCut == AppScreenType.Uninstall.screenName -> {
                        Intent(this@BaseSplashActivity, UninstallActivityHost::class.java).apply {
                            val bundle = Bundle().apply {
                                putString(
                                    AppShortCut.KEY_SHORTCUT_TARGET_SCREEN,
                                    targetScreenFromShortCut
                                )
                            }
                            putExtras(bundle)
                        }
                    }

                    /**Những case shortcut khác*/
                    targetScreenFromShortCut?.isNotBlank() == true -> {
                        Intent(this@BaseSplashActivity, MainActivity::class.java).apply {
                            val bundle = Bundle().apply {
                                putString(
                                    AppShortCut.KEY_SHORTCUT_TARGET_SCREEN,
                                    targetScreenFromShortCut
                                )
                            }
                            putExtras(bundle)
                        }
                    }

                    /**Case chưa vào màn main lần nào*/
                    getCurrentLanguageCode().isBlank() && !appPreferences.isShowIntro -> {
                        Log.d(
                            TAG,
                            "checkAbleToNextScreen: getCurrentLanguageCode() ${getCurrentLanguageCode()} appPreferences.isShowIntro ${appPreferences.isShowIntro}"
                        )
                        createSplashIntent()
                    }

                    isAlwaysShowIntroAndLanguageScreen && !purchasePreferences.isUserVip() -> {
                        createSplashIntent()
                    }

                    else -> {
                        Intent(this@BaseSplashActivity, MainActivity::class.java)
                    }
                }
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            this@BaseSplashActivity.startActivity(intent)
            finish()
        }

        if (viewModel.currentProgress >= viewModel.timeSkipAppOpenAdWhenNotAvailable && viewModel.isAdNotValidOrLoadFailed) {
            nextScreen()
            return
        }

        if (viewModel.isTimerComplete && !viewModel.isAppOpenAdLoaded && !viewModel.isAppOpenAdShowing) {
            nextScreen()
            return
        }

        if (viewModel.isAppOpenAdDismissed) {
            nextScreen()
            return
        }
    }

    private fun createSplashIntent(): Intent {
        return if (isEnableLanguageScreen) {
            timeShowIntro = System.currentTimeMillis()
            LanguageActivity.intentStart(
                this@BaseSplashActivity,
                fromSplash = true
            )
        } else if (!isEnableLanguageScreen && isEnableIntroductionScreen) {
            timeShowIntro = System.currentTimeMillis()
            Intent(this@BaseSplashActivity, OnBoardingActivity::class.java)
        } else {
            Intent(this@BaseSplashActivity, MainActivity::class.java)
        }
    }

    private fun showRequireTurnOnNetworkBottomSheetFragment() {
        showRequireTurnOnNetworkBottomSheetFragment(
            onRetry = {
                CoroutineScope(coroutineContext).launch {
                    delay(1000)
                    if (isNetworkConnected()) {
                        analyticsManager.logEvent(AnalyticsEvent.ACTION_SPLASH_RETRY_TURN_ON)
                        remoteConfigRepository.fetchAndActive()
                    } else {
                        viewModel.showRequireTurnOnNetworkWhenRetryClickedFlow.emit(true)
                        val intentNetwork = if (Build.VERSION.SDK_INT >= 29) {
                            Intent("android.settings.panel.action.INTERNET_CONNECTIVITY")
                        } else {
                            Intent("android.settings.WIRELESS_SETTINGS")
                        }
                        openInternetConnectivityLauncher.launch(intentNetwork)
                    }
                }
            },
            onCancel = {
                remoteConfigRepository.fetchAndActive()
            }
        )
    }

}