package com.tici.vpn.proxy.master.proxy

import com.tici.vpn.proxy.master.home.HomeFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
class ProxyConnection {

    val updateListener = ArrayList<ISuperVpnProxyUpdate>()

    var vpnState = HomeFragment.DISCONNECTED

    fun setProxyUpdate(update: ISuperVpnProxyUpdate) {
        updateListener.add(update)
    }

    fun updateUI(state: String) {
        vpnState = state
        updateListener.forEach {
            it.updateUI(state)
        }
    }

}