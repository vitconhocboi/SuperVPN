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
import com.core.analytics.AnalyticsEvent
import com.core.baseui.ext.collectFlowOn
import com.core.config.data.FetchRemoteConfigState
import com.core.preference.SharedPrefs
import com.core.utilities.getCurrentLanguageCode
import com.core.utilities.hideNavigationBar
import com.core.utilities.manager.isNetworkConnected
import com.core.utilities.util.Timber
import com.tici.vpn.proxy.master.core.base_ui.CoreActivity
import com.tici.vpn.proxy.master.feature.feature_language.ui.LanguageActivity
import com.tici.vpn.proxy.master.feature.feature_onboarding.ui.helper.OnBoardingConfigFactory
import com.tici.vpn.proxy.master.feature.feature_uninstall.ui.UninstallActivityHost
import com.tici.vpn.proxy.master.main.MainActivity
import com.tici.vpn.proxy.master.required.GetDataFromRemoteUseCaseImpl
import com.tici.vpn.proxy.master.required.shortcut.AppScreenType
import com.tici.vpn.proxy.master.required.shortcut.AppShortCut
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject


private const val TAG = "BaseSplashActivity"

/** Hard fallback (ms) để điều hướng nếu remote config bị treo không trả về. */
private const val FETCH_FALLBACK_TIMEOUT_MS = 10_000L

/** Ngưỡng (ms) fetch remote config được coi là chậm → hiện loading. */
private const val SHOW_LOADING_THRESHOLD_MS = 500L

abstract class BaseSplashActivity<VB : ViewBinding> : CoreActivity<VB>() {


    private val viewModel by viewModels<SplashViewModel>()

    private var timeShowIntro by SharedPrefs.instance.preference(
        defaultValue = 0L,
        key = "timeShowIntro"
    )

    private var openInternetConnectivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    /**Shortcut Data - Điều hướng màn hình theo shortcut*/
    private val targetScreenFromShortCut by lazy {
        intent.extras?.getString(AppShortCut.KEY_SHORTCUT_TARGET_SCREEN, "")
    }

    @Inject
    lateinit var getDataFromRemoteUseCase: GetDataFromRemoteUseCaseImpl

    private var hasNavigated = false

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

        hideNavigationBar()
        initView()
        initData()
    }

    abstract fun initData()

    /** Hiển thị UI loading, chỉ gọi khi fetch remote config chậm hơn [SHOW_LOADING_THRESHOLD_MS]. */
    abstract fun showLoading()

    fun onDataReady() {
        val eventName = if (isNetworkConnected()) {
            AnalyticsEvent.NETWORK_AVAILABLE
        } else {
            AnalyticsEvent.NETWORK_NOT_AVAILABLE
        }
        analyticsManager.logEvent(eventName)

        if (isNetworkConnected()) {
            startFetchRemoteConfig()
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

    override fun onResume() {
        super.onResume()
        viewModel.isActivityResume = true
        if (viewModel.needHandleEventWhenResume) {
            viewModel.needHandleEventWhenResume = false
            showRequireTurnOnNetworkBottomSheetFragment()
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
                    navigateNextScreen()
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

    private fun startFetchRemoteConfig() {
        remoteConfigRepository.fetchAndActive()

        // Chỉ hiện loading nếu fetch chậm, tránh nháy UI khi config trả về nhanh
        CoroutineScope(coroutineContext).launch {
            delay(SHOW_LOADING_THRESHOLD_MS)
            if (!hasNavigated) showLoading()
        }

        // Hard fallback: nếu fetch bị treo không trả về, vẫn điều hướng sau một khoảng thời gian
        CoroutineScope(coroutineContext).launch {
            delay(FETCH_FALLBACK_TIMEOUT_MS)
            if (!hasNavigated) {
                Timber.e("Remote config fetch fallback timeout reached, navigate anyway")
                navigateNextScreen()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.isActivityResume = false
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancelChildren()
    }

    private fun navigateNextScreen() {
        if (hasNavigated || isFinishing || isDestroyed) return
        hasNavigated = true

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
                        "navigateNextScreen: getCurrentLanguageCode() ${getCurrentLanguageCode()} appPreferences.isShowIntro ${appPreferences.isShowIntro}"
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

    private fun createSplashIntent(): Intent {
        return if (isEnableLanguageScreen) {
            timeShowIntro = System.currentTimeMillis()
            LanguageActivity.intentStart(
                this@BaseSplashActivity,
                fromSplash = true
            )
        } else if (!isEnableLanguageScreen && isEnableIntroductionScreen) {
            timeShowIntro = System.currentTimeMillis()
            Intent(
                this@BaseSplashActivity,
                OnBoardingConfigFactory.getOnBoardingClass(getDataFromRemoteUseCase.onBoardingConfig)
            )
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
                        startFetchRemoteConfig()
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
                startFetchRemoteConfig()
            }
        )
    }

}
