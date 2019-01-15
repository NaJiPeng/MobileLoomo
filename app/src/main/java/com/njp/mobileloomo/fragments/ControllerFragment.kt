package com.njp.mobileloomo.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.njp.mobileloomo.R
import com.njp.mobileloomo.databinding.FragmentControllerBinding
import com.njp.mobileloomo.robot.MobileConnectionManager
import com.njp.mobileloomo.robot.Timer
import com.njp.mobileloomo.utils.ConnectEvent
import com.njp.mobileloomo.utils.ToastUtil
import com.njp.mobileloomo.views.RockerView
import com.segway.robot.mobile.sdk.connectivity.BufferMessage
import com.segway.robot.mobile.sdk.connectivity.StringMessage
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.nio.ByteBuffer

class ControllerFragment : Fragment() {

    private lateinit var mBinding: FragmentControllerBinding
    private lateinit var mTimerBase: Timer
    private lateinit var mTimerHead: Timer
    private var lv = 0.5f
    private var av = 0.0f
    private var pv = 0.5f
    private var yv = 0.0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_controller, container, false)

        mTimerBase = Timer(100) {
            MobileConnectionManager.send(StringMessage("content|base_velocity:$lv:$av"))
        }

        mTimerHead = Timer(100) {
            MobileConnectionManager.send(StringMessage("content|head_velocity:$pv:$yv"))
        }

        MobileConnectionManager.setMessageReceiveListener {
            Log.i("mmmm", "${it.timestamp}")
            when (it) {
                is BufferMessage -> {
                    Log.i("mmmm", "${it.timestamp}")

                }
            }
        }

        mBinding.rockerBase.setOnAngleChangeListener(object : RockerView.OnAngleChangeListener {
            override fun onStart() {
                mTimerBase.start()
            }

            override fun angle(angle: Double) {
                if (angle >= 45 && angle < 135) {//后
                    lv = -50f / 100.0f
                    av = 0f
                } else if (angle >= 135 && angle < 225) {//左
                    lv = 0f
                    av = 50f / 100.0f
                } else if (angle > 225 && angle < 315) {//前
                    lv = 50f / 100.0f
                    av = 0f
                } else {//右
                    lv = 0f
                    av = -50f / 100.0f
                }
            }

            override fun onFinish() {
                mTimerBase.stop()
            }

        })

        mBinding.rockerHead.setOnAngleChangeListener(object : RockerView.OnAngleChangeListener {
            override fun onStart() {
                mTimerHead.start()
            }

            override fun angle(angle: Double) {
                if (angle >= 45 && angle < 135) {//下
                    pv = -50f / 100.0f
                    yv = 0f
                } else if (angle >= 135 && angle < 225) {//左
                    pv = 0f
                    yv = 50f / 100.0f
                } else if (angle > 225 && angle < 315) {//上
                    pv = 50f / 100.0f
                    yv = 0f
                } else {//右
                    pv = 0f
                    yv = -50f / 100.0f
                }
            }

            override fun onFinish() {
                MobileConnectionManager.send(StringMessage("content|head_velocity:0:0"))
                mTimerHead.stop()
            }

        })

        mBinding.imgSpeak.setOnClickListener {
            var editText: EditText?
            AlertDialog.Builder(context)
                    .setMessage("请输入你想说的话：")
                    .setView(EditText(context).apply { editText = this })
                    .setPositiveButton("确定") { dialogInterface: DialogInterface, i: Int ->
                        val content = editText?.text?.toString()
                        if (content.isNullOrEmpty()) {
                            ToastUtil.show("内容不能为空！")
                        } else {
                            MobileConnectionManager.send(StringMessage("content|speak:$content")) {
                                ToastUtil.show(if (it) "发送成功" else "发送失败")
                                dialogInterface.dismiss()
                            }
                        }
                    }.setNegativeButton("取消") { dialogInterface: DialogInterface, i: Int ->
                        dialogInterface.dismiss()
                    }
                    .show()
        }

        mBinding.imgReset.setOnClickListener {
            AlertDialog.Builder(context)
                    .setMessage("是否重设起点，并清空已保存的路径点？")
                    .setPositiveButton("确定") { dialogInterface: DialogInterface, i: Int ->
                        MobileConnectionManager.send(StringMessage("content|base_reset")) {
                            ToastUtil.show(if (it) "重置成功" else "重置失败")
                        }
                        dialogInterface.dismiss()
                    }.setNegativeButton("取消") { dialogInterface: DialogInterface, i: Int ->
                        dialogInterface.dismiss()
                    }
                    .show()
        }

        mBinding.imgAdd.setOnClickListener {
            val editText: EditText?
            AlertDialog.Builder(context)
                    .setMessage("请输入路径点名称：")
                    .setView(EditText(context).apply {
                        editText = this
                    })
                    .setPositiveButton("确定") { dialogInterface: DialogInterface, i: Int ->
                        val name = editText?.text?.trim()
                        if (name.isNullOrBlank()){
                            ToastUtil.show("路径点名称不能为空！")
                            return@setPositiveButton
                        }
                        MobileConnectionManager.send(StringMessage("content|base_add:$name")) {
                            ToastUtil.show(if (it) "保存成功" else "保存失败")
                        }
                        dialogInterface.dismiss()
                    }.setNegativeButton("取消") { dialogInterface: DialogInterface, i: Int ->
                        dialogInterface.dismiss()
                    }
                    .show()
        }

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        MobileConnectionManager.send(StringMessage("mode|control"))

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
            MobileConnectionManager.send(StringMessage("mode|control"))
        }
    }


}