package com.njp.mobileloomo.robot

import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.MulticastSocket

/**
 * 接收来自Loomo机器人的IP地址广播的线程
 */
object IPBroadcastReceiver {

    private val mPort = 10001

    private val datagramSocket = DatagramSocket(mPort)

    fun receive(listener: (String) -> Unit) {
        Thread {
            try{
                val buffer = ByteArray(1024)
                val datagramPacket = DatagramPacket(buffer, buffer.size)
                datagramSocket.receive(datagramPacket)
                listener.invoke(datagramPacket.address.hostAddress)
            }catch (e:Exception){
                Log.e("mmmm","error",e)
                //DO NOTHING
            }
        }.start()
    }

}