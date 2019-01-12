package com.njp.mobileloomo.robot

import java.net.DatagramPacket
import java.net.DatagramSocket

/**
 * 接收来自Loomo机器人的IP地址广播的线程
 */
object IPReceiver {

    private val mPort = 10001
    private val datagramSocket = DatagramSocket(mPort)
    var isAlive = true

    fun receive() {
        Thread {
            try {
                val buffer = ByteArray(1024)
                val datagramPacket = DatagramPacket(buffer, buffer.size)
                datagramSocket.receive(datagramPacket)
                if (!MobileConnectionManager.isConnect && isAlive) {
                    MobileConnectionManager.connect(datagramPacket.address.hostAddress)
                }
            } catch (e: Exception) {
                //DO NOTHING
            }
        }.start()
    }
}
