package com.changjiashuai.paritysigner.utils

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.changjiashuai.paritysigner.models.AlertState
import io.parity.signer.uniffi.historyGetWarnings

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/19 22:36.
 */
object AirPlaneUtils {


    fun isAirplaneOn(context: Context): Boolean {
        return Settings.System.getInt(
            context.contentResolver,
            Settings.System.AIRPLANE_MODE_ON,
            0
        ) == 1
    }

    /**
     * get alert state.
     *
     * if airplane mode is on: [AlertState.Active],
     * if airplane mode is off, then if [historyGetWarnings] is true: [AlertState.Past] else [AlertState.None]
     *
     */
    fun getAlertState(context: Context): AlertState {
        return if (isAirplaneOn(context)) {
            //airplane mode
            if (historyGetWarnings(DbUtils.dbName)) {
                AlertState.Past
            } else {
                AlertState.None
            }
        } else {
            AlertState.Active
        }
    }

    fun toggleAirPlaneMode(context: Context) {
        Settings.System.putInt(
            context.contentResolver,
            Settings.System.AIRPLANE_MODE_ON,
            if (isAirplaneOn(context)) 0 else 1
        )
    }

    fun sendAirPlaneModeChangedBroadcast(context: Context) {
        val intent = Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED).apply {
            putExtra("state", !isAirplaneOn(context))
        }
        context.sendBroadcast(intent)
    }
}