package com.njp.mobileloomo.robot

import android.annotation.SuppressLint
import android.content.Context
import com.njp.mobileloomo.MyApplication
import com.segway.robot.mobile.sdk.connectivity.MobileException
import com.segway.robot.mobile.sdk.connectivity.MobileMessageRouter
import com.segway.robot.mobile.sdk.connectivity.StringMessage
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.baseconnectivity.Message
import com.segway.robot.sdk.baseconnectivity.MessageConnection
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * 管理与Loomo连接通讯的类
 */
object MobileConnectionManager {

    private var isBind = false
    private val bindStateListener = object : ServiceBinder.BindStateListener {
        override fun onBind() {
            isBind = true
            try {
                mobileMessageRouter.register {
                    messageConnection = it
                    try {
                        messageConnection?.setListeners(object : MessageConnection.ConnectionStateListener {
                            override fun onOpened() {
                                connectionStateListener?.onNext(true)
                                isConnect = true
                            }

                            override fun onClosed(error: String?) {
                                connectionStateListener?.onNext(false)
                                isConnect = false
                            }

                        }, object : MessageConnection.MessageListener {
                            override fun onMessageSentError(message: Message<*>?, error: String?) {
                                message?.let {
                                    messageSendListeners[message.id]?.onError(Throwable(error))
                                    messageSendListeners.remove(message.id)
                                }
                            }

                            override fun onMessageSent(message: Message<*>?) {
                                message?.let {
                                    messageSendListeners[message.id]?.onNext(it.id)
                                    messageSendListeners.remove(it.id)
                                }
                            }

                            override fun onMessageReceived(message: Message<*>?) {
                                message?.let {
                                    messageReceiveListener?.onNext(it)
                                }
                            }
                        })
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: MobileException) {
                e.printStackTrace()
            }

        }

        override fun onUnbind(reason: String) {
            isBind = false
        }
    }
    private var messageConnection: MessageConnection? = null
    var isConnect = false
    private var messageReceiveListener: ObservableEmitter<Message<*>>? = null
    private var connectionStateListener: ObservableEmitter<Boolean>? = null
    private val messageSendListeners = HashMap<Int, ObservableEmitter<Int>>()

    private val mobileMessageRouter = MobileMessageRouter.getInstance()

    @SuppressLint("CheckResult")
    fun send(message: Message<*>, listener: ((Boolean) -> Unit)? = null) {
        if (!isConnect) {
            listener?.invoke(false)
            return
        }
        Observable.create(ObservableOnSubscribe<Int> {
            messageSendListeners[message.id] = it
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            listener?.invoke(true)
                        },
                        {
                            listener?.invoke(false)
                        }
                )
        messageConnection?.sendMessage(message)
    }

    @SuppressLint("CheckResult")
    fun setMessageReceiveListener(listener: (Message<*>) -> Unit) {
        Observable.create(ObservableOnSubscribe<Message<*>> {
            messageReceiveListener = it
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    listener.invoke(it)
                }
    }

    @SuppressLint("CheckResult")
    fun setConnectStateListener(listener: (Boolean) -> Unit) {
        Observable.create(ObservableOnSubscribe<Boolean> {
            connectionStateListener = it
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    listener.invoke(it)
                }
    }

    fun connect(ip: String) {
        if (isBind) {
            mobileMessageRouter.unbindService()
        }
        mobileMessageRouter.setConnectionIp(ip)
        mobileMessageRouter.bindService(MyApplication.instance, bindStateListener)
    }

    fun unBind() {
        if (isBind) {
            mobileMessageRouter.unbindService()
        }
    }

}