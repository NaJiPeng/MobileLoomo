package com.njp.mobileloomo.robot

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * 发送连续控制信息的工具类
 */
class Timer(time: Long, private val listener: () -> Unit) {

    private var mIsAlive = false


    private val disposable = Observable.interval(0, time, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe {
                if (mIsAlive) {
                    listener.invoke()
                }
            }


    fun start() {
        mIsAlive = true
    }

    fun stop() {
        mIsAlive = false
    }

    fun dispose(){
        disposable.dispose()
    }

}

