package com.hoangsinh.supervpn.network

import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketException

/**
 * Created by metehan on 4/26/2016.
 */
class DatagramSocketThread : Thread() {
    private var mDatagramSocket: DatagramSocket? = null
    private var mDatagramPacket: DatagramPacket? = null
    private var socketAlive: Boolean = false

    override fun run() {
        try {
            mDatagramSocket = DatagramSocket(InetSocketAddress("127.0.0.1", 8087))
            val buffer = ByteArray(32767)
            mDatagramPacket = DatagramPacket(buffer, buffer.size)
            socketAlive = true

            while (socketAlive) {
                mDatagramPacket!!.length = buffer.size
                mDatagramSocket!!.receive(mDatagramPacket)
                println("DatagramSocket at 8087 received packet from VpnService")
                println("Address: " + mDatagramPacket!!.address + " Port: " + mDatagramPacket!!.port)
                println("Length of data: " + mDatagramPacket!!.data.size)

//                var tcpPacket: TCPPacket = TCPPacket(0, mDatagramPacket!!.data)
//
//                System.out.println("Sending from socket -> TCP Source port: " + tcpPacket.getSourcePort())
//                System.out.println("Sending from socket -> TCP Destination port: " + tcpPacket.getDestinationPort())
//                System.out.println("Sending from socket -> TCP Destination address: " + tcpPacket.getDestinationAddressAsLong())
//                System.out.println("Sending from socket -> TCP Length of data: " + tcpPacket.getData().length)

                // Send packet
                mDatagramSocket!!.send(mDatagramPacket)

                // Receive packet
                mDatagramPacket!!.length = buffer.size
                mDatagramSocket!!.receive(mDatagramPacket)
                mDatagramPacket!!.socketAddress = InetSocketAddress("127.0.0.1", 8087)
                mDatagramSocket!!.send(mDatagramPacket)

                println("Receiving from socket -> DatagramPacket Source port: " + mDatagramPacket!!.port)
                println("Receiving from socket -> DatagramPacket Destination port: " + mDatagramPacket!!.address)
                println("Receiving from socket -> DatagramPacket Destination address: " + mDatagramPacket!!.socketAddress)
                println("Receiving from socket -> DatagramPacket Length of data: " + mDatagramPacket!!.data.size)

//                tcpPacket = TCPPacket(0, mDatagramPacket!!.data)
//                System.out.println("Receiving from socket -> TCP Source port: " + tcpPacket.getSourcePort())
//                System.out.println("Receiving from socket -> TCP Destination port: " + tcpPacket.getDestinationPort())
//                System.out.println("Receiving from socket -> TCP Destination address: " + tcpPacket.getDestinationAddressAsLong())
//                System.out.println("Receiving from socket -> TCP Length of data: " + tcpPacket.getData().length)
            }
        } catch (e: SocketException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun sendPacket(packet: DatagramPacket) {
        packet.data
    }
}