package com.njp.mobileloomo.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
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
import com.segway.robot.mobile.sdk.connectivity.StringMessage
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ControllerFragment : Fragment() {

    private lateinit var mBinding: FragmentControllerBinding
    private lateinit var mTimer: Timer
    private var lv = 0.5f
    private var av = 0.0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_controller, container, false)

        mTimer = Timer(100) {
            MobileConnectionManager.send(StringMessage("content@base_raw:$lv:$av"))
        }

        mBinding.rockerBase.setOnAngleChangeListener(object : RockerView.OnAngleChangeListener {
            override fun onStart() {
                mTimer.start()
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
                mTimer.stop()
            }

        })


        mBinding.imgClear.setOnClickListener {
            AlertDialog.Builder(context)
                    .setMessage("重新设置起点将清空所有路径点，是否继续？")
                    .setPositiveButton("确定") { dialogInterface: DialogInterface, _: Int ->
                        MobileConnectionManager.send(StringMessage("content@base_clear")) {
                            ToastUtil.show(if (it) "清除路径点成功" else "清除路径点失败")
                        }
                        dialogInterface.dismiss()
                    }
                    .setNegativeButton("取消") { dialogInterface: DialogInterface, i: Int ->
                        dialogInterface.dismiss()
                    }
                    .show()
        }

        mBinding.imgAdd.setOnClickListener {
            var editText: EditText?
            AlertDialog.Builder(context)
                    .setMessage("请设置当前路径点名称：")
                    .setView(EditText(context).apply { editText = this })
                    .setPositiveButton("确定") { dialogInterface: DialogInterface, i: Int ->
                        val name = editText?.text?.toString()
                        if (name.isNullOrBlank()) {
                            ToastUtil.show("路径点名称不能为空！")
                        } else {
                            MobileConnectionManager.send(StringMessage("content@base_add:$name")) {
                                ToastUtil.show(if (it) "添加路径点成功" else "添加路径点失败")
                            }
                            dialogInterface.dismiss()
                        }
                    }.setNegativeButton("取消") { dialogInterface: DialogInterface, i: Int ->
                        dialogInterface.dismiss()
                    }
                    .show()
        }

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
                            MobileConnectionManager.send(StringMessage("content@speak_content:$content")) {
                                ToastUtil.show(if (it) "发送成功" else "发送失败")
                                dialogInterface.dismiss()
                            }
                        }
                    }.setNegativeButton("取消") { dialogInterface: DialogInterface, i: Int ->
                        dialogInterface.dismiss()
                    }
                    .show()
        }

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        MobileConnectionManager.send(StringMessage("mode@control"))

        return mBinding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this)
        }
    }


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun controlModeOn(event: ConnectEvent) {
        if (event.isConnect) {
            MobileConnectionManager.send(StringMessage("mode@control"))
        }
    }


}