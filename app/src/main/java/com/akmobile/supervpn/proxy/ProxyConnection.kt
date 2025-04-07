package com.akmobile.supervpn.proxy

import com.akmobile.supervpn.home.HomeFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import timber.log.Timber

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