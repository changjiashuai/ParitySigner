package com.changjiashuai.paritysigner.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.changjiashuai.paritysigner.OnBoardingState
import com.changjiashuai.paritysigner.utils.DbUtils
import com.changjiashuai.paritysigner.utils.PrefsUtils
import io.parity.signer.uniffi.*
import java.io.File
import java.io.FileOutputStream

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/6/25 21:15.
 */
class MainViewModel : AbsViewModel() {

    private val _onBoardingDone = MutableLiveData(OnBoardingState.InProgress)
    val onBoardingState: LiveData<OnBoardingState> = _onBoardingDone

    /**
     * Populate database!
     * This is normal onboarding
     */
    fun onBoard() {
        wipe()
        DbUtils.copyAsset("")
        historyInitHistoryWithCert(DbUtils.dbName)
        //notify update ui
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

    fun totalRefresh() {
        val checkRefresh = File(DbUtils.dbName).exists()
        if (checkRefresh) {
            _onBoardingDone.value = OnBoardingState.Yes
        } else {
            _onBoardingDone.value = OnBoardingState.No
        }
        if (checkRefresh) {
            refreshSeedNames(init = true)
            pushButton(Action.START)
        }
    }

    fun deviceWasOnline() {
        historyDeviceWasOnline(DbUtils.dbName)
    }
}