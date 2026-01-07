package com.tici.vpn.proxy.master.required.preferences

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoreAppPreferences @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
) {
    companion object {
        private const val SHARED_NAME = "private_shared_app"

        private const val KEY_LAST_TIME_CONNECTED_PROXY = "KEY_LAST_TIME_CONNECTED_PROXY"

        private const val KEY_ENABLE_RATE_LOGIC = "KEY_ENABLE_RATE_LOGIC"

        private const val KEY_NUMBER_CONNECTED_PROXY = "KEY_NUMBER_CONNECTED_PROXY"
    }

    private val prefs: SharedPreferences =
        applicationContext.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE)


    var lastTimeConnectedProxy by prefs.long(
        key = { KEY_LAST_TIME_CONNECTED_PROXY },
        defaultValue = 0L
    )

    var numberConnectedProxy by prefs.long(
        key = { KEY_NUMBER_CONNECTED_PROXY },
        defaultValue = 0L
    )

    var isEnableRateLogic by prefs.boolean(
        key = { KEY_ENABLE_RATE_LOGIC },
        defaultValue = true
    )


}