package com.common.baseui

import com.common.baseui.lingver.Lingver
import com.simple.libads.AdsApplication

abstract class BaseApplication: AdsApplication() {

    companion object {
        lateinit var instance: BaseApplication
    }

    override fun onCreate() {
        instance = this
        super.onCreate()
        Lingver.init(this, BaseAppConfig.languageCode)
        if(BaseAppConfig.languageCode.isNotEmpty()) {
            Lingver.getInstance().setLocale(this, BaseAppConfig.languageCode)
        } else {
            Lingver.getInstance().setFollowSystemLocale(this)
        }
    }
}