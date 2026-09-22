package com.core.config.data

import android.content.Context
import android.util.Log
import com.core.analytics.AnalyticsEvent
import com.core.analytics.AnalyticsManager
import com.core.config.data.mapper.AppConfigModelMapper
import com.core.config.data.mapper.IapConfigModelMapper
import com.core.config.domain.GetDataFromRemoteConfigUseCase
import com.core.config.domain.RemoteConfigRepository
import com.core.config.domain.data.AppConfig
import com.core.config.domain.data.AppConfig.Companion.DEFINE_INTRO_HAVE_ADS
import com.core.config.domain.data.AppConfig.Companion.DEFINE_INTRO_NO_ADS
import com.core.config.domain.data.IapConfig
import com.core.preference.AppPreferences
import com.core.utilities.isDebug
import com.core.utilities.toast
import com.core.utilities.util.toast.Toasty
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RemoteConfigRepositoryImpl @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val analyticsManager: AnalyticsManager,
    private val appPreferences: AppPreferences,
    private val appConfigModelMapper: AppConfigModelMapper,
    private val iapConfigModelMapper: IapConfigModelMapper,
    private val remoteConfigService: RemoteConfigService,
    private val getRemoteConfigUseCase: GetDataFromRemoteConfigUseCase,
) : RemoteConfigRepository {

    companion object {
        const val TAG = "RemoteConfigRepository"
        const val FETCH_REMOTE_CONFIG_TIMEOUT = 10 * 1000L
    }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _fetchStateCompleteFlow = MutableSharedFlow<FetchRemoteConfigState>()
    override val fetchStateCompleteFlow = _fetchStateCompleteFlow.asSharedFlow()

    private var isFetchComplete = false
    private var isFetching = false

    override fun fetchAndActive() {
        if (isFetching) {
            return
        }
        isFetching = true
        isFetchComplete = false
        applicationScope.launch {
            _fetchStateCompleteFlow.emit(FetchRemoteConfigState.Loading)
        }
        analyticsManager.logEvent(AnalyticsEvent.EVENT_REMOTE_CONFIG_FETCH)
        Log.e(TAG, "fetch loading")
        remoteConfigService.fetchAndActive { isSuccess ->
            isFetching = false
            if (applicationContext.isDebug() /*|| BuildConfig.FLAVOR == "dev"*/) {
                applicationContext.toast("fetch RemoteConfig Successfully!", Toasty.SUCCESS)
            }
            if (!isFetchComplete) {
                Log.e(TAG, "fetch complete $isSuccess")
                fetchRemoteConfigData(isNotifyComplete = true, isSuccess = true)
            } else {
                fetchRemoteConfigData(isNotifyComplete = false, isSuccess = true)
            }
            isFetchComplete = true
        }
        applicationScope.launch {
            delay(FETCH_REMOTE_CONFIG_TIMEOUT)
            if (!isFetchComplete) {
                analyticsManager.logEvent(AnalyticsEvent.EVENT_REMOTE_CONFIG_FETCH_TIMEOUT)
                if (appPreferences.isRemoteConfigFirstTimeFetch) {
                    appPreferences.isRemoteConfigFirstTimeFetch = false
                    analyticsManager.logEvent(AnalyticsEvent.EVENT_REMOTE_CONFIG_FETCH_TIMEOUT_FIRST)
                }
                if (applicationContext.isDebug() /*|| BuildConfig.FLAVOR == "dev"*/) {
                    applicationContext.toast("fetch RemoteConfig Timeout!", Toasty.WARNING)
                }
                Log.e(TAG, "fetch complete timeout")
                fetchRemoteConfigData(isNotifyComplete = true, isSuccess = false)
            }
            isFetchComplete = true
        }
    }

    private fun fetchRemoteConfigData(isNotifyComplete: Boolean, isSuccess: Boolean) {
        applicationScope.launch {
            val appConfigDeferred = async { getAppConfigRaw() }
            val getOtherConfig = async {
                getRemoteConfigUseCase.invoke(remoteConfigService)
            }

            appConfigCache = appConfigDeferred.await()
            getOtherConfig.await()

            if (isNotifyComplete) {
                _fetchStateCompleteFlow.emit(FetchRemoteConfigState.Complete(isSuccess))
            }
        }
    }

    private var appConfigCache: AppConfig? = null
    private var iapConfigCache: IapConfig? = null

    private fun getAppConfigRaw(): AppConfig {
        val model = remoteConfigService.getAppConfig()
        return if (model == null) {
            AppConfig(
                isHideNavigationBar = false,
                isAlwaysShowIntroAndLanguageScreen = false,
                isEnableIntroductionScreen = true,
                isEnableChangeLanguageScreen = true,
                isEnableAppShortCut = false,
                isEnableAppShortcutUninstall = false,
                introActionShowType = 1,
                introData = arrayListOf(DEFINE_INTRO_HAVE_ADS,DEFINE_INTRO_HAVE_ADS,DEFINE_INTRO_HAVE_ADS),
                intervalDayAlwaysShowIntroAndLanguage = 3,
                isAlwaysShowIntroAndLanguageScreenWithInterval = false,
                introDataV2 = arrayListOf(DEFINE_INTRO_NO_ADS,DEFINE_INTRO_NO_ADS,DEFINE_INTRO_HAVE_ADS)
            )
        } else {
            appConfigModelMapper.toData(model)
        }
    }

    private fun getIapConfigRaw(): IapConfig {
        val model = remoteConfigService.getIapConfig()
        return if (model == null) {
            IapConfig(
                isShowIAPOnStart = false,
                isShowIAPFirstOpen = true,
                isShowIAPBeforeRequestPermission = true,
                isEnableIapV2 = true,
                timeWaitToShowCloseIcon = 1500,
                upgradePremiumDisableByCountry = listOf()
            )
        } else {
            iapConfigModelMapper.toData(model)
        }
    }

    override fun getAppConfig(): AppConfig {
        return appConfigCache ?: getAppConfigRaw()
    }

    override fun getIapConfig(): IapConfig {
        return iapConfigCache ?: getIapConfigRaw()
    }
}
