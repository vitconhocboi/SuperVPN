package com.hoangsinh.supervpn.network

import android.util.Log
import com.hoangsinh.supervpn.network.tcpip.CommonMethods
import com.hoangsinh.supervpn.network.tunnel.Tunnel
import com.hoangsinh.supervpn.utils.Constant
import java.lang.String
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

class TcpProxyServer(port: Int) : Runnable {
    var Stopped: Boolean = false
    var Port: Short = 0

    var m_Selector: Selector?
    var m_ServerSocketChannel: ServerSocketChannel?
    var m_ServerThread: Thread? = null

    init {
        m_Selector = Selector.open()
        m_ServerSocketChannel = ServerSocketChannel.open()
        m_ServerSocketChannel?.let {
            it.configureBlocking(false)
            it.socket().bind(InetSocketAddress(port))
            it.register(m_Selector, SelectionKey.OP_ACCEPT)
            this.Port = it.socket().localPort.toShort()
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
        this.Stopped = true
        if (m_Selector != null) {
            try {
                m_Selector!!.close()
            } catch (e: Exception) {
                Log.e(Constant.TAG, "Exception when closing m_Selector", e)
            } finally {
                m_Selector = null
            }
        }

        if (m_ServerSocketChannel != null) {
            try {
                m_ServerSocketChannel!!.close()
            } catch (e: Exception) {
                Log.e(Constant.TAG, "Exception when closing m_ServerSocketChannel", e)
            } finally {
                m_ServerSocketChannel = null
            }
        }
    }

    override fun run() {
        try {
            while (true) {
                m_Selector!!.select()
                val keyIterator = m_Selector!!.selectedKeys().iterator()
                while (keyIterator.hasNext()) {
                    val key = keyIterator.next()
                    if (key.isValid) {
                        try {
                            if (key.isReadable) {
                                (key.attachment() as Tunnel).onReadable(key)
                            } else if (key.isWritable) {
                                (key.attachment() as Tunnel).onWritable(key)
                            } else if (key.isConnectable) {
                                (key.attachment() as Tunnel).onConnectable()
                            } else if (key.isAcceptable) {
                                onAccepted(key)
                            }
                        } catch (e: Exception) {
                            Log.d(Constant.TAG, e.toString())
                        }
                    }
                    keyIterator.remove()
                }
            }
        } catch (e: Exception) {
            Log.e(Constant.TAG, "TcpServer", e)
        } finally {
            this.stop()
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

    fun onAccepted(key: SelectionKey?) {
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
