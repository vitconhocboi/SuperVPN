package com.tici.vpn.proxy.master.utils

import android.util.Log
import timber.log.Timber

class ReleaseTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Only log WARN, ERROR, or ASSERT in release
        if (priority == Log.ERROR || priority == Log.WARN || priority == Log.ASSERT) {
            Log.println(priority, tag ?: "ReleaseLog", message)
        }
    }

    override fun i(message: String?, vararg args: Any?) {
        Log.println(Log.INFO, "ReleaseLog", message.toString())
    }
}