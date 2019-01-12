package com.njp.mobileloomo

import android.app.ProgressDialog
import android.content.DialogInterface
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Gravity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.njp.mobileloomo.databinding.ActivityMainBinding
import com.njp.mobileloomo.robot.IPReceiver
import com.njp.mobileloomo.robot.MobileConnectionManager
import com.njp.mobileloomo.utils.ConnectEvent
import com.njp.mobileloomo.utils.ToastUtil
import org.greenrobot.eventbus.EventBus

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mNavController: NavController
    private lateinit var mProgressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        mProgressDialog = ProgressDialog(this).apply {
            setMessage("连接中...")
        }

        mNavController = (supportFragmentManager.findFragmentById(R.id.fragment_host) as NavHostFragment).navController

        binding.imgMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(Gravity.START)
        }
        MobileConnectionManager.setConnectStateListener {
            if (it) {
                mProgressDialog.dismiss()
                binding.imgConnection.setImageResource(R.drawable.ic_connect)
                EventBus.getDefault().post(ConnectEvent(true))
                ToastUtil.show("已连接")
            } else {
                binding.imgConnection.setImageResource(R.drawable.ic_disconnect)
                EventBus.getDefault().post(ConnectEvent(false))
                ToastUtil.show("连接已断开")
            }
        }

        NavigationUI.setupWithNavController(binding.navigationView, mNavController)

        binding.imgConnection.setOnClickListener {
            if (MobileConnectionManager.isConnect) {
                AlertDialog.Builder(this)
                        .setMessage("断开连接？")
                        .setPositiveButton("确定") { dialogInterface: DialogInterface, _: Int ->
                            MobileConnectionManager.unBind()
                            IPReceiver.isAlive = false
                            dialogInterface.dismiss()
                        }
                        .setNegativeButton("取消") { dialogInterface: DialogInterface, _: Int ->
                            dialogInterface.dismiss()
                        }
                        .show()
            } else {
                IPReceiver.isAlive = true
                mProgressDialog.show()
                IPReceiver.receive()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MobileConnectionManager.unBind()
    }

}
