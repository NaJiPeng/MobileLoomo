package com.njp.mobileloomo.fragments

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.njp.mobileloomo.MainActivity
import com.njp.mobileloomo.R
import com.njp.mobileloomo.databinding.FragmentPatrolBinding
import com.njp.mobileloomo.robot.MobileConnectionManager
import com.segway.robot.mobile.sdk.connectivity.StringMessage

class PatrolFragment : Fragment() {

    private lateinit var binding: FragmentPatrolBinding
    private lateinit var mConnectManager: MobileConnectionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_patrol, container, false)
        mConnectManager = (activity as MainActivity).mConnectionManager


        Log.i("mmmm","onCreateView")
        mConnectManager.setMessageReceiveListener {
            Log.i("mmmm", (it as StringMessage).content)
        }
        mConnectManager.send(StringMessage("base_get"))
        Log.i("mmmm", "create")

        return binding.root
    }

}