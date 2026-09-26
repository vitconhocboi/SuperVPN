package com.tici.vpn.proxy.master.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VpnStartRequestTest {

    @Test
    fun userStartIsStickySoProcessDeathRestartsIt() {
        val r = VpnStartRequest.from(LocalVpnService.ACTION_START)
        assertEquals(VpnStartRequest.USER_START, r)
        assertTrue(r.sticky)
    }

    @Test
    fun stopIsNeverSticky() {
        val r = VpnStartRequest.from(LocalVpnService.ACTION_STOP)
        assertEquals(VpnStartRequest.STOP, r)
        assertFalse(r.sticky)
    }

    @Test
    fun alwaysOnSystemIntentStartsSession() {
        // android.net.VpnService.SERVICE_INTERFACE, what Always-on VPN sends.
        assertEquals(VpnStartRequest.SYSTEM_START, VpnStartRequest.from("android.net.VpnService"))
    }

    @Test
    fun nullIntentFromStickyRestartStartsSession() {
        val r = VpnStartRequest.from(null)
        assertEquals(VpnStartRequest.SYSTEM_START, r)
        assertTrue(r.sticky)
    }

    @Test
    fun unknownActionIsIgnoredAndNotSticky() {
        val r = VpnStartRequest.from("com.example.SOMETHING_ELSE")
        assertEquals(VpnStartRequest.UNKNOWN, r)
        assertFalse(r.sticky)
    }
}
