package com.akmobile.supervpn.network

import android.util.Log
import com.akmobile.supervpn.network.tcpip.CommonMethods
import com.akmobile.supervpn.network.tunnel.Tunnel
import com.akmobile.supervpn.utils.Constant
import java.lang.String
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

class TcpProxyServer(port: Int) : Runnable {
    var Stopped: Boolean = false
    var Port: Short = 0

    private var m_Selector: Selector?
    private var m_ServerSocketChannel: ServerSocketChannel?
    private var m_ServerThread: Thread? = null

    init {
        m_Selector = Selector.open()
        m_ServerSocketChannel = ServerSocketChannel.open()
        m_ServerSocketChannel?.let {
            it.configureBlocking(false)
            it.socket().bind(InetSocketAddress(port))
            it.register(m_Selector, SelectionKey.OP_ACCEPT)
            Port = it.socket().localPort.toShort()
        }
        Log.d(Constant.TAG, "AsyncTcpServer listen on " + (Port.toInt() and 0xFFFF))
    }

    @Synchronized
    fun start() {
        m_ServerThread = Thread(this)
        m_ServerThread!!.name = "TcpProxyServerThread"
        m_ServerThread!!.start()
    }

    @Synchronized
    fun stop() {
        Stopped = true
        m_Selector?.wakeup()
        
        m_ServerSocketChannel?.let {
            try {
                it.close()
            } catch (e: Exception) {
                Log.e(Constant.TAG, "Error closing server socket", e)
            }
        }
        m_ServerSocketChannel = null

        m_Selector?.let {
            try {
                it.close()
            } catch (e: Exception) {
                Log.e(Constant.TAG, "Error closing selector", e)
            }
        }
        m_Selector = null
    }

    override fun run() {
        try {
            while (!Stopped) {
                val readyChannels = m_Selector!!.select(250)
                
                if (readyChannels == 0) continue
                
                val selectedKeys = m_Selector!!.selectedKeys()
                val keyIterator = selectedKeys.iterator()
                
                while (keyIterator.hasNext()) {
                    val key = keyIterator.next()
                    try {
                        when {
                            !key.isValid -> {
                                key.cancel()
                            }
                            key.isAcceptable -> {
                                onAccepted(key)
                            }
                            key.isReadable -> {
                                (key.attachment() as Tunnel).onReadable(key)
                            }
                            key.isWritable -> {
                                (key.attachment() as Tunnel).onWritable(key)
                            }
                            key.isConnectable -> {
                                (key.attachment() as Tunnel).onConnectable()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(Constant.TAG, "Error handling key: ${e.message}", e)
                        key.cancel()
                        try {
                            key.channel()?.close()
                        } catch (ignored: Exception) {}
                    } finally {
                        keyIterator.remove()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(Constant.TAG, "TcpServer error", e)
        } finally {
            stop()
            Log.d(Constant.TAG, "TcpServer thread exited.")
        }
    }

    fun getDestAddress(localChannel: SocketChannel): InetSocketAddress? {
        val portKey = localChannel.socket().port.toShort()
        val session = NatSessionManager.getSession(portKey.toInt())
        if (session != null) {
            if (ProxyConfig.Instance.needProxy(session.RemoteHost)) {
                if (ProxyConfig.IS_DEBUG) Log.d(
                    Constant.TAG, String.format(
                        "%d/%d:[PROXY] %s=>%s:%d",
                        NatSessionManager.sessionCount,
                        Tunnel.SessionCount,
                        session.RemoteHost,
                        CommonMethods.ipIntToString(session.RemoteIP),
                        session.RemotePort.toInt() and 0xFFFF
                    )
                )
                return InetSocketAddress.createUnresolved(
                    session.RemoteHost, session.RemotePort.toInt() and 0xFFFF
                )
            } else {
                return InetSocketAddress(
                    localChannel.socket().inetAddress, session.RemotePort.toInt() and 0xFFFF
                )
            }
        }
        return null
    }

    private fun onAccepted(key: SelectionKey?) {
        var localTunnel: Tunnel? = null
        try {
            val localChannel = m_ServerSocketChannel!!.accept()
            localTunnel = TunnelFactory.wrap(localChannel, m_Selector)

            val destAddress = getDestAddress(localChannel)
            if (destAddress != null) {
                val remoteTunnel: Tunnel =
                    TunnelFactory.createTunnelByConfig(destAddress, m_Selector)
                remoteTunnel.setBrotherTunnel(localTunnel)
                localTunnel.setBrotherTunnel(remoteTunnel)
                remoteTunnel.connect(destAddress)
            } else {
                LocalVpnService.Instance.writeLog(
                    "Error: socket(%s:%d) target host is null.",
                    localChannel.socket().inetAddress.toString(),
                    localChannel.socket().port
                )
                localTunnel.dispose()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LocalVpnService.Instance.writeLog(
                "Error: remote socket create failed: %s", e.toString()
            )
            if (localTunnel != null) {
                localTunnel.dispose()
            }
        }
    }
}
