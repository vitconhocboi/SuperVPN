package com.tici.vpn.proxy.master.network

import android.net.VpnService

/**
 * How [LocalVpnService.onStartCommand] treats an incoming start intent. Pure mapping (the
 * constants are compile-time inlined), so it is JVM-unit-testable.
 *
 * @property sticky whether the service asks the system to restart it after a process kill.
 */
enum class VpnStartRequest(val sticky: Boolean) {
    /** User pressed disconnect. */
    STOP(false),

    /** User pressed connect; consent was obtained by the UI. */
    USER_START(true),

    /**
     * Started by the system: Always-on VPN ([VpnService.SERVICE_INTERFACE]) or a sticky restart
     * after process death (null intent). No UI, so consent must already exist.
     */
    SYSTEM_START(true),

    /** Anything else: ignored. */
    UNKNOWN(false);

    companion object {
        fun from(action: String?): VpnStartRequest = when (action) {
            LocalVpnService.ACTION_STOP -> STOP
            LocalVpnService.ACTION_START -> USER_START
            VpnService.SERVICE_INTERFACE, null -> SYSTEM_START
            else -> UNKNOWN
        }
    }
}
