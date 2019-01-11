package com.njp.mobileloomo.fragments

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.njp.mobileloomo.MainActivity
import com.njp.mobileloomo.R
import com.njp.mobileloomo.adapter.PointsAdapter
import com.njp.mobileloomo.databinding.FragmentPatrolBinding
import com.njp.mobileloomo.robot.MobileConnectionManager
import com.segway.robot.mobile.sdk.connectivity.StringMessage

class PatrolFragment : Fragment() {

    private lateinit var mBinding: FragmentPatrolBinding
    private lateinit var mConnectManager: MobileConnectionManager
    private lateinit var mAdapter: PointsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_patrol, container, false)
        mAdapter = PointsAdapter {
            mConnectManager.send(StringMessage("base_point:$it"))
        }

        mBinding.recyclerView.layoutManager = GridLayoutManager(context, 5)
        mBinding.recyclerView.adapter = mAdapter

        mConnectManager.addStringMessageReceiveListener("points") {
            mAdapter.setData(it)
        }
        mConnectManager.send(StringMessage("base_get"))

        return mBinding.root
    }

}