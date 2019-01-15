package com.njp.mobileloomo.fragments

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.njp.mobileloomo.R
import com.njp.mobileloomo.adapter.ChatAdapter
import com.njp.mobileloomo.databinding.FragmentChatBinding
import com.njp.mobileloomo.robot.MobileConnectionManager
import com.njp.mobileloomo.utils.ConnectEvent
import com.njp.mobileloomo.utils.ToastUtil
import com.segway.robot.mobile.sdk.connectivity.StringMessage
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var adapter: ChatAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)
        adapter = ChatAdapter()

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.itemAnimator = SlideInUpAnimator()


        binding.btnChat.setOnClickListener {
            val text = binding.etChat.text.toString()
            if (text.isEmpty()) {
                ToastUtil.show("聊天内容不能为空！")
                return@setOnClickListener
            }
            MobileConnectionManager.send(StringMessage("content|chat:$text")) {
                if (it) {
                    ToastUtil.show("发送成功！")
                    adapter.add("man:${binding.etChat.text}")
                    binding.recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
                    binding.etChat.text.clear()
                } else {
                    ToastUtil.show("发送失败！")
                }
            }
        }

        MobileConnectionManager.setMessageReceiveListener {
            when (it) {
                is StringMessage -> {
                    Log.i("mmmm", it.content)
                    adapter.add(it.content)
                    binding.recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
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