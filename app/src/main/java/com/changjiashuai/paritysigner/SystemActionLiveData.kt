package com.changjiashuai.paritysigner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.LiveData

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/23 14:18.
 */
class SystemActionLiveData(private val context: Context, vararg actions: String) :
    LiveData<String>() {

    private var intentFilter: IntentFilter = IntentFilter()

    init {
        actions.forEach {
            intentFilter.addAction(it)
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (action.isNullOrEmpty()) {
                return
            }
            value = action
        }
    }

    override fun onActive() {
        super.onActive()
        this.context.registerReceiver(receiver, intentFilter)
    }

    override fun onInactive() {
        super.onInactive()
        this.context.unregisterReceiver(receiver)
    }
}