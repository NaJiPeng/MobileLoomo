package com.njp.mobileloomo.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.support.v7.app.AlertDialog
import android.content.DialogInterface
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import com.njp.mobileloomo.R
import com.njp.mobileloomo.databinding.FragmentControllerBinding
import com.njp.mobileloomo.robot.MobileConnectionManager
import com.njp.mobileloomo.robot.Timer
import com.njp.mobileloomo.utils.ConnectEvent
import com.njp.mobileloomo.utils.ToastUtil
import com.njp.mobileloomo.views.RockerView
import com.segway.robot.mobile.sdk.connectivity.BufferMessage
import com.segway.robot.mobile.sdk.connectivity.StringMessage
import com.tbruyelle.rxpermissions2.RxPermissions
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.io.FileOutputStream

class ControllerFragment : Fragment() {

    private lateinit var mBinding: FragmentControllerBinding
    private lateinit var mTimerBase: Timer
    private lateinit var mTimerHead: Timer
    private var lv = 0.5f
    private var av = 0.0f
    private var pv = 0.5f
    private var yv = 0.0f
    private var ts = 0L
    private lateinit var rxPermissions: RxPermissions

    private val children = listOf("Scissors", "Rock", "Paper")

    private var isRecording = false
    private var isClassify = false
    private val parent = File(Environment.getExternalStorageDirectory(), "Loomo").apply {
        if (!exists()) {
            mkdir()
        }
    }
    private var child = ""

    @SuppressLint("CheckResult")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_controller, container, false)
        rxPermissions = RxPermissions(this)
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe { granted ->
                    if (granted) {
                        if (!parent.exists()) {
                            parent.mkdir()
                        }
                    }
                }

        mTimerBase = Timer(100) {
            MobileConnectionManager.send(StringMessage("content|base_velocity:$lv:$av"))
        }

        mTimerHead = Timer(100) {
            MobileConnectionManager.send(StringMessage("content|head_velocity:$pv:$yv"))
        }

        MobileConnectionManager.setMessageReceiveListener {
            when (it) {
                is StringMessage -> {
                    val data = it.content.split(":")
                    when (data[0]) {
                        "classify" -> {
                            mBinding.tvClassify.text = it.content
                            Log.i("mmmm",it.content)
                        }
                    }
                }

                is BufferMessage -> {
                    if (it.timestamp > ts) {
                        val data = it.content
                        if (isRecording) {
                            if (it.timestamp > ts + 1000) {
                                val dir = File(parent, child).apply {
                                    if (!exists()) {
                                        mkdir()
                                    }
                                }
                                val file = File(dir, "${it.timestamp}.jpg")
                                val out = FileOutputStream(file)
                                out.write(data)
                                out.flush()
                            }
                        }
                        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                        mBinding.imgCamera.setImageBitmap(bitmap)
                        ts = it.timestamp
                    }
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
                mTimerHead.stop()
            }

        })

        mBinding.imgSpeak.setOnClickListener {
            var editText: EditText?
            AlertDialog.Builder(context!!)
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
            AlertDialog.Builder(context!!)
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
            AlertDialog.Builder(context!!)
                    .setMessage("请输入路径点名称：")
                    .setView(EditText(context).apply {
                        editText = this
                    })
                    .setPositiveButton("确定") { dialogInterface: DialogInterface, i: Int ->
                        val name = editText?.text?.trim()
                        if (name.isNullOrBlank()) {
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

        mBinding.imgPhoto.setOnClickListener {
            if (!isClassify) {
                mBinding.tvClassify.visibility = View.VISIBLE
                MobileConnectionManager.send(StringMessage("content|classify:true"))
                isClassify = true
                ToastUtil.show("开始进行物体识别")
                mBinding.imgPhoto.setImageResource(R.drawable.ic_stop)
            } else {
                mBinding.tvClassify.visibility = View.INVISIBLE
                MobileConnectionManager.send(StringMessage("content|classify:false"))
                isClassify = false
                ToastUtil.show("停止进行物体识别")
                mBinding.imgPhoto.setImageResource(R.drawable.ic_start)
            }
//            var radioGroup: RadioGroup?
//            if (!isRecording) {
//                AlertDialog.Builder(context!!)
//                        .setMessage("请选择采集物品名称：")
//                        .setView(RadioGroup(context).apply {
//                            radioGroup = this
//                            children.forEachIndexed { i: Int, s: String ->
//                                addView(RadioButton(context).apply {
//                                    id = i
//                                    text = s
//                                    if (i == 0) {
//                                        isChecked = true
//                                    }
//                                })
//                            }
//                        }).setPositiveButton("开始采集") { dialogInterface: DialogInterface, i: Int ->
//                            rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                                    .subscribe { granted ->
//                                        if (granted){
//                                            child = children[radioGroup!!.checkedRadioButtonId]
//                                            ToastUtil.show("开始采集")
//                                            mBinding.imgPhoto.setImageResource(R.drawable.ic_stop)
//                                            isRecording = true
//                                        }else{
//                                            ToastUtil.show("未授权")
//                                        }
//                                    }
//                            dialogInterface.dismiss()
//                        }
//                        .show()
//            } else {
//                ToastUtil.show("停止采集")
//                mBinding.imgPhoto.setImageResource(R.drawable.ic_start)
//                isRecording = false
//            }
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