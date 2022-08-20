package com.changjiashuai.paritysigner.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.changjiashuai.paritysigner.OnBoardingState
import com.changjiashuai.paritysigner.SystemActionLiveData
import com.changjiashuai.paritysigner.models.AlertState
import com.changjiashuai.paritysigner.utils.DbUtils
import com.changjiashuai.paritysigner.utils.PrefsUtils
import io.parity.signer.uniffi.*
import java.io.File

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/18 21:34.
 */
open class AbsViewModel : ViewModel() {

    // Alert
//    private val _alertState: MutableLiveData<AlertState> = MutableLiveData(AlertState.None)
//    val alertState: LiveData<AlertState> = _alertState

    private val _actionResult = MutableLiveData(
        ActionResult(
            screenLabel = "",
            back = false,
            footer = false,
            footerButton = null,
            rightButton = null,
            screenNameType = ScreenNameType.H4,
            screenData = ScreenData.Documents,
            modalData = null,
            alertData = null,
        )
    )
    val actionResult: LiveData<ActionResult> = _actionResult

    fun pushButton(
        button: Action,
        details: String = "",
        seedPhrase: String = ""
    ) {
        try {
            Log.i("AbsViewModel", "push button=$button, details=$details, seedPhrase=$seedPhrase")
            _actionResult.value = backendAction(button, details, seedPhrase)
            Log.i("AbsViewModel", "push button after _actionResult=${_actionResult.value}")
        } catch (e: Exception) {
            Log.e("Navigation error", e.toString())
        }
    }


    // Seeds
    private val _seedNames = MutableLiveData(arrayOf<String>())
    val seedNames: LiveData<Array<String>> = _seedNames

    fun refreshSeedNames(init: Boolean = false) {
        val allNames = PrefsUtils.sharedPreferences.all.keys.sorted().toTypedArray()
        if (init) {
            initNavigation(DbUtils.dbName, allNames.toList())
        } else {
            updateSeedNames(allNames.toList())
        }
        Log.i("AbsViewModel", "seedNames=$allNames")
        _seedNames.value = allNames
    }
}