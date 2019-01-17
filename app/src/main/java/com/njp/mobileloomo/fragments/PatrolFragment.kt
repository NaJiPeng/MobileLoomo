package com.njp.mobileloomo.fragments

import android.databinding.DataBindingUtil
import android.databinding.ObservableBoolean
import android.os.Bundle
import android.support.design.chip.Chip
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.njp.mobileloomo.R
import com.njp.mobileloomo.adapter.PointsAdapter
import com.njp.mobileloomo.bean.Coor2D
import com.njp.mobileloomo.databinding.FragmentPatrolBinding
import com.njp.mobileloomo.robot.MobileConnectionManager
import com.njp.mobileloomo.utils.ConnectEvent
import com.njp.mobileloomo.utils.ToastUtil
import com.segway.robot.mobile.sdk.connectivity.StringMessage
import jp.wasabeef.recyclerview.animators.SlideInDownAnimator
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class PatrolFragment : Fragment() {

    private lateinit var mBinding: FragmentPatrolBinding
    private lateinit var mAdapter: PointsAdapter
    private val mLoop = ObservableBoolean(false)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_patrol, container, false)
        mBinding.loop = mLoop
        mAdapter = PointsAdapter()

        mBinding.recyclerView.layoutManager = GridLayoutManager(context, 4)
        mBinding.recyclerView.adapter = mAdapter
        mBinding.recyclerView.itemAnimator = SlideInDownAnimator()

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
                                                mAdapter.add(point)
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }

        mBinding.btnDelete.setOnClickListener {
            mAdapter.remove()
        }

        mBinding.btnStart.setOnClickListener {
            MobileConnectionManager.send(StringMessage("content|base_patrol:${mAdapter.getPonits()}:${mLoop.get()}")){
                ToastUtil.show(if (it) "开始巡逻模式" else "巡逻模式失败")
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