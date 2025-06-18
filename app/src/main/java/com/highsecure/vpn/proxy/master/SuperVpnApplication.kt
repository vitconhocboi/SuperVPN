package com.highsecure.vpn.proxy.master

import android.annotation.SuppressLint
import com.common.baseui.BaseAppConfig
import com.common.baseui.BaseApplication
import com.highsecure.vpn.proxy.master.splash.SplashActivity
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class SuperVpnApplication : BaseApplication() {

    companion object {
        lateinit var instance: SuperVpnApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Timber.d("check_open_splash 0 ${System.currentTimeMillis()}")
    }


    override fun isInitAds(): Boolean {
        return currentActivity !is SplashActivity && !BaseAppConfig.firstTimeSetup && !BaseAppConfig.isSub
    }
}