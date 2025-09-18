package com.common.baseui

import android.app.Application

abstract class BaseApplication: Application() {

    companion object {
        lateinit var instance: Application
            private set

        fun attachInstance(app: Application) {
            instance = app
        }
    }
}