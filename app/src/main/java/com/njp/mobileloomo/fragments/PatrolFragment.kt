package com.njp.mobileloomo.fragments

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.chip.Chip
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.njp.mobileloomo.R
import com.njp.mobileloomo.bean.Coor2D
import com.njp.mobileloomo.databinding.FragmentPatrolBinding
import com.njp.mobileloomo.robot.MobileConnectionManager
import com.njp.mobileloomo.utils.ConnectEvent
import com.segway.robot.mobile.sdk.connectivity.StringMessage
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class PatrolFragment : Fragment() {

    private lateinit var mBinding: FragmentPatrolBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_patrol, container, false)


        MobileConnectionManager.setMessageReceiveListener {
            when (it) {
                is StringMessage -> {
                    val data = it.content.split("|")
                    when (data[0]) {
                        "points" -> {
                            val list = Gson().fromJson<List<Coor2D>>(data[1], object : TypeToken<List<Coor2D>>() {}.type)
                            list.forEach { point ->
                                mBinding.chipGroup.addView(
                                        Chip(context).apply {
                                            text = point.name
                                            setOnClickListener {
                                                MobileConnectionManager.send(StringMessage("content|base_nav:${point.id}"))
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }


        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        MobileConnectionManager.send(StringMessage("mode|patrol"))

        return mBinding.root
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
            MobileConnectionManager.send(StringMessage("mode|patrol"))
        }
    }

}