package com.highsecure.vpn.proxy.master

import com.common.baseui.BaseAppConfig
import com.common.baseui.BaseApplication
import com.highsecure.vpn.proxy.master.splash.SplashActivity
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SuperVpnApplication : BaseApplication() {

    companion object {
        lateinit var instance: SuperVpnApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }


    override fun isInitAds(): Boolean {
        return currentActivity !is SplashActivity && !BaseAppConfig.firstTimeSetup
    }
}