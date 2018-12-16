package com.njp.mobileloomo.utils

import android.view.Gravity
import android.widget.Toast
import com.njp.mobileloomo.MyApplication

object ToastUtil {

    private val toast = Toast.makeText(MyApplication.instance, "", Toast.LENGTH_SHORT).apply {
        setGravity(Gravity.TOP, 0, 250)
    }

    fun show(content: String) {
        toast.setText(content)
        toast.show()
    }

}