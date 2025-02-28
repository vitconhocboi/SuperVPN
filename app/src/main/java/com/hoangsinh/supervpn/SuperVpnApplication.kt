package com.hoangsinh.supervpn

import com.common.baseui.BaseAppConfig
import com.common.baseui.BaseApplication
import com.hoangsinh.supervpn.splash.SplashActivity
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