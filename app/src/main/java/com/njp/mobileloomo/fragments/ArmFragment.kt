package com.njp.mobileloomo.fragments

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.njp.mobileloomo.R
import com.njp.mobileloomo.databinding.FragmentArmBinding
import com.njp.mobileloomo.robot.MobileConnectionManager
import com.njp.mobileloomo.utils.ConnectEvent
import com.segway.robot.mobile.sdk.connectivity.StringMessage
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ArmFragment : Fragment() {

    private lateinit var mBinding: FragmentArmBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_arm, container, false)

        MobileConnectionManager.setMessageReceiveListener {
            when (it) {
                is StringMessage -> {
//                    val data = it.content.split(":")
//                    when (data[0]) {
//                        "classify" -> {
//                            mBinding.tvClassify.text = it.content
//                        }
//                    }
                }
                else -> {

                }
            }
        }

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        MobileConnectionManager.send(StringMessage("mode|arm"))
        return mBinding.root
    }


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun controlModeOn(event: ConnectEvent) {
        if (event.isConnect) {
            MobileConnectionManager.send(StringMessage("mode|arm"))
        }
    }

}