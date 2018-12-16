package com.njp.mobileloomo.fragments

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.njp.mobileloomo.R
import com.njp.mobileloomo.databinding.FragmentAutoBinding

class AutoFragment : Fragment() {

    private lateinit var binding: FragmentAutoBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_auto, container, false)
        return binding.root
    }

}