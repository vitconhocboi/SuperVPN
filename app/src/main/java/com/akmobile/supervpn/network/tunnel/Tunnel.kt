package com.akmobile.supervpn.network.tunnel

import android.annotation.SuppressLint
import android.util.Log
import com.akmobile.supervpn.network.LocalVpnService
import timber.log.Timber
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

abstract class Tunnel {
    protected var m_DestAddress: InetSocketAddress? = null
    private var m_InnerChannel: SocketChannel?
    private var m_SendRemainBuffer: ByteBuffer? = null
    private var m_Selector: Selector?
    private var m_BrotherTunnel: Tunnel? = null
    private var m_Disposed = false
    private var m_ServerEP: InetSocketAddress? = null

    constructor(innerChannel: SocketChannel?, selector: Selector?) {
        this.m_InnerChannel = innerChannel
        this.m_Selector = selector
        SessionCount++
    }

    constructor(serverAddress: InetSocketAddress?, selector: Selector?) {
        val innerChannel = SocketChannel.open()
        innerChannel.configureBlocking(false)
        this.m_InnerChannel = innerChannel
        this.m_Selector = selector
        this.m_ServerEP = serverAddress
        SessionCount++
    }

    @Throws(Exception::class)
    protected abstract fun onConnected(buffer: ByteBuffer?)

    protected abstract fun isTunnelEstablished(): Boolean

    @Throws(Exception::class)
    protected abstract fun beforeSend(buffer: ByteBuffer?)

    @Throws(Exception::class)
    protected abstract fun afterReceived(buffer: ByteBuffer?)

    protected abstract fun onDispose()

    fun setBrotherTunnel(brotherTunnel: Tunnel?) {
        m_BrotherTunnel = brotherTunnel
    }

    @Throws(Exception::class)
    fun connect(destAddress: InetSocketAddress?) {
        if (LocalVpnService.Instance.protect(m_InnerChannel!!.socket())) {
            m_DestAddress = destAddress
            m_InnerChannel!!.register(m_Selector, SelectionKey.OP_CONNECT, this)
            m_Selector?.wakeup()
            m_InnerChannel!!.connect(m_ServerEP)
        } else {
            throw Exception("VPN protect socket failed.")
        }
    }

    @Throws(Exception::class)
    protected fun beginReceive() {
        if (m_InnerChannel!!.isBlocking) {
            m_InnerChannel!!.configureBlocking(false)
        }
        m_InnerChannel!!.register(m_Selector, SelectionKey.OP_READ, this)
        m_Selector?.wakeup()
    }


    @Throws(Exception::class)
    protected fun write(buffer: ByteBuffer?, copyRemainData: Boolean): Boolean {
        var bytesSent: Int
        while (buffer!!.hasRemaining()) {
            bytesSent = m_InnerChannel!!.write(buffer)
            if (bytesSent == 0) {
                break
            }
        }

        if (buffer.hasRemaining()) {
            if (copyRemainData) {
                if (m_SendRemainBuffer == null) {
                    m_SendRemainBuffer = ByteBuffer.allocate(buffer.capacity())
                }
                m_SendRemainBuffer!!.clear()
                m_SendRemainBuffer!!.put(buffer)
                m_SendRemainBuffer!!.flip()
                m_InnerChannel!!.register(m_Selector, SelectionKey.OP_WRITE, this)
                m_Selector?.wakeup()
            }
            return false
        } else {
            return true
        }
    }

    @Throws(Exception::class)
    protected fun onTunnelEstablished() {
        this.beginReceive()
        m_BrotherTunnel!!.beginReceive()
    }

    @SuppressLint("DefaultLocale")
    fun onConnectable() {
        try {
            if (m_InnerChannel!!.finishConnect()) {
                onConnected(ByteBuffer.allocate(2048))
            } else {
                Timber.d("Error: connect to %s failed. ${m_ServerEP.toString()}")
                this.dispose()
            }
        } catch (e: Exception) {
            Timber.d("Error: connect to ${m_ServerEP.toString()} failed: ${e.printStackTrace()}")
            this.dispose()
        }
    }

    fun onReadable(key: SelectionKey) {
        try {
            val buffer = ByteBuffer.allocate(2048)
            buffer.clear()
            val bytesRead = m_InnerChannel!!.read(buffer)
            if (bytesRead > 0) {
                buffer.flip()
                afterReceived(buffer)
                if (isTunnelEstablished() && buffer.hasRemaining()) {
                    m_BrotherTunnel!!.beforeSend(buffer)
                    if (!m_BrotherTunnel!!.write(buffer, true)) {
                        key.cancel()
                        Log.d(
                            "Tunnel",
                            m_ServerEP.toString() + "can not read more."
                        )
                    }
                }
            } else if (bytesRead < 0) {
                this.dispose()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            this.dispose()
        }
    }

    fun onWritable(key: SelectionKey) {
        try {
            this.beforeSend(m_SendRemainBuffer)
            if (this.write(m_SendRemainBuffer, false)) {
                key.cancel()
                if (isTunnelEstablished()) {
                    m_BrotherTunnel!!.beginReceive()
                } else {
                    this.beginReceive()
                }
            }
        } catch (e: Exception) {
            this.dispose()
        }
    }

    fun dispose() {
        disposeInternal(true)
    }

    fun disposeInternal(disposeBrother: Boolean) {
        if (m_Disposed) {
            return
        } else {
            try {
                m_InnerChannel!!.close()
            } catch (e: Exception) {
            }

            if (m_BrotherTunnel != null && disposeBrother) {
                m_BrotherTunnel!!.disposeInternal(false)
            }

            m_InnerChannel = null
            m_SendRemainBuffer = null
            m_Selector = null
            m_BrotherTunnel = null
            m_Disposed = true
            SessionCount--

            onDispose()
        }
    }

    companion object {
        var SessionCount: Long = 0
    }
}
