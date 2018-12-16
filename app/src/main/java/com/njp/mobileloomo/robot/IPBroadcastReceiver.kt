package com.njp.mobileloomo.robot

import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket

/**
 * 接收来自Loomo机器人的IP地址广播的线程
 */
class IPBroadcastReceiver {

    private val mHost = "239.0.0.1"
    private val mPort = 10001

    private val multicastSocket = MulticastSocket(mPort).apply {
        joinGroup(InetAddress.getByName(mHost))
    }


    fun receive(listener: (String) -> Unit) {
        Thread {
            val buffer = ByteArray(1024)
            val datagramPacket = DatagramPacket(buffer, buffer.size)
            multicastSocket.receive(datagramPacket)
            listener.invoke(datagramPacket.address.hostAddress)
        }.start()
    }

}