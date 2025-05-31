package com.highsecure.vpn.proxy.master.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.highsecure.vpn.proxy.master.network.LocalVpnService
import timber.log.Timber

class SchedulerReceiver : BroadcastReceiver() {

    companion object {
        const val STOP_PROXY = "super-vpn.STOP_PROXY"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Timber.i("receive broadcast context")
        when (intent?.action) {
            STOP_PROXY -> {
                Timber.i("receive call stopProxy")
                LocalVpnService.stopProxy(context!!)
            }
        }
    }
}