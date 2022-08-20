package com.changjiashuai.paritysigner.viewmodel

import android.annotation.SuppressLint
import com.changjiashuai.paritysigner.utils.DbUtils
import com.changjiashuai.paritysigner.utils.PrefsUtils
import io.parity.signer.uniffi.Action
import java.io.File

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/19 23:04.
 */
class SettingsViewModel : AbsViewModel() {

    /**
     * Auth user and wipe the Signer to initial state
     */
    fun wipeToFactory() {
        wipe()
        totalRefresh()
    }

    /**
     * Wipes all data
     */
    @SuppressLint("ApplySharedPref")
    fun wipe() {
        DbUtils.deleteDir(File(DbUtils.dbName))
        PrefsUtils.sharedPreferences.edit().clear().commit() // No, not apply(), do it now!
    }

    /**
     * This returns the app into starting state; should be called
     * on all "back"-like events and new screen spawns just in case
     */
    fun totalRefresh() {
        val checkRefresh = File(DbUtils.dbName).exists()
//        if (checkRefresh) {
//            _onBoardingDone.value = OnBoardingState.Yes
//        } else {
//            _onBoardingDone.value = OnBoardingState.No
//        }
        if (checkRefresh) {
//            getAlertState()
//            isAirplaneOn()
            refreshSeedNames(init = true)
            doAction(Action.START)
        }
    }
}