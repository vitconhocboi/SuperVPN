package com.common.baseui

import com.common.baseui.lingver.Lingver
import com.simple.libads.AdsApplication
import timber.log.Timber

abstract class BaseApplication: AdsApplication() {

    companion object {
        lateinit var instance: BaseApplication
    }

    override fun onCreate() {
        instance = this
        super.onCreate()
        val start = System.currentTimeMillis()
        Lingver.init(this, BaseAppConfig.languageCode)
        if(BaseAppConfig.languageCode.isNotEmpty()) {
            Lingver.getInstance().setLocale(this, BaseAppConfig.languageCode)
        } else {
            Lingver.getInstance().setFollowSystemLocale(this)
        }
        val end = System.currentTimeMillis()
        Timber.d("check_load_splash ${end - start} time ${System.currentTimeMillis()}")
    }
}