package com.njp.mobileloomo.fragments

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.njp.mobileloomo.R
import com.njp.mobileloomo.databinding.FragmentChatBinding
import com.njp.mobileloomo.robot.MobileConnectionManager
import com.njp.mobileloomo.utils.ConnectEvent
import com.segway.robot.mobile.sdk.connectivity.StringMessage
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)



        MobileConnectionManager.setMessageReceiveListener {
            when (it) {
                is StringMessage -> {
                    Log.i("mmmm", it.content)
                    val data = it.content.split(":")
                    when (data[0]) {
                        "man" -> {

                        }
                        "robot" -> {

                        }
                    }
                }
                else -> {

                }
            }
        }






        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        MobileConnectionManager.send(StringMessage("mode|chat"))
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun controlModeOn(event: ConnectEvent) {
        if (event.isConnect) {
            MobileConnectionManager.send(StringMessage("mode|chat"))
        }
    }

}