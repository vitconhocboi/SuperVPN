package com.tici.vpn.proxy.master.network

import com.tici.vpn.proxy.master.home.HomeFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Observer of the VPN connect state. Implemented by whatever drives the Home UI.
 *
 * Was `proxy.ISuperVpnProxyUpdate`; moved into `network/` because it is tunnel-state
 * plumbing, not remote-proxy logic, and the `proxy/` package is being removed.
 */
interface VpnStateObserver {
    fun updateUI(status: String)
}

/**
 * Fan-out of the VPN connect state from [LocalVpnService] to the UI layer.
 *
 * Was `proxy.ProxyConnection`. Single instance provided by Hilt; the service pushes
 * CONNECTED/DISCONNECTED and every registered [VpnStateObserver] is notified.
 */
@ExperimentalCoroutinesApi
@FlowPreview
@Singleton
class VpnStateListener @Inject constructor() {

    val updateListener = ArrayList<VpnStateObserver>()

    var vpnState = HomeFragment.DISCONNECTED

    fun setProxyUpdate(update: VpnStateObserver) {
        updateListener.add(update)
    }

    fun updateUI(state: String) {
        vpnState = state
        updateListener.forEach {
            it.updateUI(state)
        }
    }

}
