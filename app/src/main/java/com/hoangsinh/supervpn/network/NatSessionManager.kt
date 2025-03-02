package com.hoangsinh.supervpn.network

import android.util.SparseArray
import com.hoangsinh.supervpn.network.tcpip.CommonMethods

object NatSessionManager {
    const val MAX_SESSION_COUNT: Int = 4096
    const val SESSION_TIMEOUT_NS: Long = 120 * 1000000000L
    val Sessions: SparseArray<NatSession> = SparseArray()

    fun getSession(portKey: Int): NatSession {
        return Sessions[portKey]
    }

    val sessionCount: Int
        get() = Sessions.size()

    fun clearExpiredSessions() {
        val now = System.nanoTime()
        for (i in Sessions.size() - 1 downTo 0) {
            val session = Sessions.valueAt(i)
            if (now - session.LastNanoTime > SESSION_TIMEOUT_NS) {
                Sessions.removeAt(i)
            }
        }
    }

    fun clearAllSessions() {
        Sessions.clear()
    }

    fun createSession(portKey: Int, remoteIP: Int, remotePort: Short): NatSession {
        if (Sessions.size() > MAX_SESSION_COUNT) {
            clearExpiredSessions()
        }

        val session = NatSession()
        session.LastNanoTime = System.nanoTime()
        session.RemoteIP = remoteIP
        session.RemotePort = remotePort
        if (session.RemoteHost == null) {
            session.RemoteHost = CommonMethods.ipIntToString(remoteIP)
        }
        Sessions.put(portKey, session)
        return session
    }
}
