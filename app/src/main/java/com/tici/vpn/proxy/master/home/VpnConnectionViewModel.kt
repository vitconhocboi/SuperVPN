package com.tici.vpn.proxy.master.home

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tici.vpn.proxy.master.network.LocalVpnService
import com.tici.vpn.proxy.master.network.LocalVpnService.Companion.ACTION_START
import com.tici.vpn.proxy.master.network.LocalVpnService.Companion.ACTION_STOP
import com.tici.vpn.proxy.master.network.VpnStateListener
import com.tici.vpn.proxy.master.network.VpnStateObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

/**
 * Owns the VPN connect state for Home and drives [LocalVpnService].
 *
 * Replaces the connect/disconnect half of the old `ProxyViewModel`, minus the remote
 * proxy assignment and the connect/disconnect session reporting API — there is no
 * remote server to assign or report to any more.
 */
@HiltViewModel
class VpnConnectionViewModel @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@Inject constructor(
    private val vpnState: VpnStateListener
) : ViewModel(), VpnStateObserver {

    val isConnected = MutableLiveData(vpnState.vpnState)

    init {
        vpnState.setProxyUpdate(this)
    }

    /** Callback from [VpnStateListener]; also used by Home to push a state directly. */
    override fun updateUI(status: String) {
        isConnected.postValue(status)
    }

    fun startVpn(context: Context?, allowApp: List<String>?) {
        val intent = Intent(context, LocalVpnService::class.java).apply {
            action = ACTION_START
            putStringArrayListExtra("allowApp", allowApp as ArrayList<String>?)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context!!.startForegroundService(intent)
        } else {
            context!!.startService(intent)
        }
    }

    fun stopVpn(context: Context?) {
        context!!.startService(Intent(context, LocalVpnService::class.java).apply {
            action = ACTION_STOP
        })
    }
}
