package com.changjiashuai.paritysigner

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.changjiashuai.paritysigner.utils.AirPlaneUtils
import com.changjiashuai.paritysigner.utils.DbUtils
import io.parity.signer.uniffi.initLogging

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/6/25 19:14.
 */
class App : Application() {

    init {
        initLogging("SIGNER_RUST_LOG")
        // actually load RustNative code
        System.loadLibrary("signer")
    }

    override fun onCreate() {
        super.onCreate()
        DbUtils.initDb(applicationContext)

//        registerAirPlaneMode()
    }

    private fun registerAirPlaneMode() {
        val intentFilter = IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        val receiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val state = intent.extras?.getBoolean("state")
                Log.i("App", "AirPlaneMode Changed=$state")
                Log.i("App", "AirPlaneMode Changed=${AirPlaneUtils.isAirplaneOn(context)}")
                airPlaneModeChangeListener?.changed(AirPlaneUtils.isAirplaneOn(context))
            }
        }
        applicationContext.registerReceiver(receiver, intentFilter)
    }

    var airPlaneModeChangeListener: AirPlaneModeChangeListener? = null

    interface AirPlaneModeChangeListener {
        fun changed(isOn: Boolean)
    }
}