package com.changjiashuai.paritysigner.models

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.changjiashuai.paritysigner.models.SignerDataModel
import io.parity.signer.uniffi.*

/**
 * This pretty much offloads all navigation to backend!
 */
internal val _actionResult = MutableLiveData(
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

fun SignerDataModel.pushButton(
    button: Action,
    details: String = "",
    seedPhrase: String = ""
) {
    try {
        _currentAction.value = button
        Log.i("pushButton", "button=$button, details=$details, seedPhrase=$seedPhrase")
        _actionResult.value = backendAction(button, details, seedPhrase)
    } catch (e: java.lang.Exception) {
        Log.e("Navigation error", e.toString())
    }
}
