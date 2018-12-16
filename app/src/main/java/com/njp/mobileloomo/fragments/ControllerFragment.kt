package com.njp.mobileloomo.fragments

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.njp.mobileloomo.MainActivity
import com.njp.mobileloomo.R
import com.njp.mobileloomo.databinding.FragmentControllerBinding
import com.njp.mobileloomo.robot.Timer
import com.njp.mobileloomo.views.RockerView
import com.segway.robot.mobile.sdk.connectivity.StringMessage

class ControllerFragment : Fragment() {

    private lateinit var mBinding: FragmentControllerBinding
    private lateinit var mTimer: Timer
    private var lv = 0.0f
    private var av = 0.0f
    private var flag = 1.0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_controller, container, false)

        mTimer = Timer(100){
            (activity as MainActivity).mConnectionManager.send(StringMessage("base:${flag * lv}:$av"))
        }

        mBinding.rockerLinear.setOnDistanceLevelListener(object : RockerView.OnDistanceLevelListener {
            override fun onDistanceLevel(level: Int) {
                lv = (level / 9.0).toFloat()
            }

        })
        mBinding.rockerLinear.setOnAngleChangeListener(object : RockerView.OnAngleChangeListener {
            override fun onStart() {
                mTimer.start()
            }

            override fun angle(angle: Double) {
                av = ((90 - angle % 180) / 90.0).toFloat()
                flag = (-Math.sin(2 * Math.PI / 360 * angle)).toFloat()
            }

            override fun onFinish() {
                mTimer.stop()
            }

        })



        return mBinding.root
    }

}