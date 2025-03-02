package com.hoangsinh.supervpn.network.tunnel.httpconnect

import android.text.TextUtils
import android.util.Base64
import android.util.Log
import com.hoangsinh.supervpn.network.ProxyConfig
import com.hoangsinh.supervpn.network.tunnel.Tunnel
import java.nio.ByteBuffer
import java.nio.channels.Selector
import java.util.Locale

class HttpConnectTunnel(config: HttpConnectConfig, selector: Selector?) :
    Tunnel(config.ServerAddress, selector) {
    var m_TunnelEstablished = false
    override fun isTunnelEstablished(): Boolean {
        return m_TunnelEstablished
    }

    private var m_FirstPacket = false
    private var m_Config: HttpConnectConfig?

    init {
        m_Config = config
    }

    @Throws(Exception::class)
    protected override fun onConnected(buffer: ByteBuffer?) {
        val request = if (TextUtils.isEmpty(m_Config!!.UserName) || TextUtils.isEmpty(
                m_Config!!.Password
            )
        ) {
            java.lang.String.format(
                Locale.ENGLISH, """
     CONNECT %s:%d HTTP/1.0
     Proxy-Connection: keep-alive
     User-Agent: %s
     X-App-Install-ID: %s
     
     
     """.trimIndent(),
                m_DestAddress?.getHostName(),
                m_DestAddress?.getPort(),
                ProxyConfig.Instance.userAgent,
                ProxyConfig.Instance.AppInstallID
            )
        } else {
            java.lang.String.format(
                Locale.ENGLISH, """
     CONNECT %s:%d HTTP/1.0
     Proxy-Authorization: Basic %s
     Proxy-Connection: keep-alive
     User-Agent: %s
     X-App-Install-ID: %s
     
     
     """.trimIndent(),
                m_DestAddress?.hostName,
                m_DestAddress?.port,
                makeAuthorization(),
                ProxyConfig.Instance.userAgent,
                ProxyConfig.Instance.AppInstallID
            )
        }
        Log.i(TAG, "onConnected: $request")
        buffer!!.clear()
        buffer.put(request.toByteArray())
        buffer.flip()
        if (this.write(buffer, true)) {
            this.beginReceive()
        }
    }

    private fun makeAuthorization(): String {
        return Base64.encodeToString(
            (m_Config!!.UserName + ":" + m_Config!!.Password).toByteArray(),
            Base64.DEFAULT
        ).trim { it <= ' ' }
    }

    @Throws(Exception::class)
    protected override fun afterReceived(buffer: ByteBuffer?) {
        if (!m_TunnelEstablished) {
            val response = String(buffer!!.array(), buffer.position(), 12)
            Log.i(TAG, "afterReceived: $response")
            if (response.matches("^HTTP/1.[01] 200$".toRegex())) {
                buffer.limit(buffer.position())
            } else {
                throw Exception(
                    String.format(
                        Locale.ENGLISH,
                        "Proxy server responsed an error: %s",
                        response
                    )
                )
            }

            m_TunnelEstablished = true
            m_FirstPacket = true
            super.onTunnelEstablished()
        } else if (m_FirstPacket) {
            // Workaround for mysterious "Content-Length: 0" after handshaking.
            // Possible a bug of golang.
            // Also need to remove "\r\n" afterward.
            var response = String(buffer!!.array(), buffer.position(), 17)
            if (response.matches("^Content-Length: 0$".toRegex())) {
                buffer.position(buffer.position() + 17)
            }
            while (true) {
                response = String(buffer.array(), buffer.position(), 2)
                if (response.matches("^\r\n$".toRegex())) {
                    buffer.position(buffer.position() + 2)
                } else {
                    break
                }
            }
            m_FirstPacket = false
        }
    }

    @Throws(Exception::class)
    protected override fun beforeSend(buffer: ByteBuffer?) {
        // Nothing
    }

    protected override fun onDispose() {
        m_Config = null
    }


    companion object {
        private const val TAG = "HttpConnectTunnel"
    }
}
