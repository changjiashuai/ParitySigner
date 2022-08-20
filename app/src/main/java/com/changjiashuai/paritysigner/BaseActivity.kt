package com.changjiashuai.paritysigner

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.changjiashuai.paritysigner.ext.showAlert
import com.changjiashuai.paritysigner.models.AlertState
import com.changjiashuai.paritysigner.utils.AirPlaneUtils
import com.changjiashuai.paritysigner.utils.DbUtils
import io.parity.signer.uniffi.*

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/3 13:04.
 */
abstract class BaseActivity : AppCompatActivity() {

    private lateinit var airPlaneModeState: SystemActionLiveData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        airPlaneModeState = SystemActionLiveData(this, Intent.ACTION_AIRPLANE_MODE_CHANGED)
        airPlaneModeState.observe(this) {
            if (!AirPlaneUtils.isAirplaneOn(this)) {
                //network is on
                historyDeviceWasOnline(DbUtils.dbName)
            }
            onAirPlaneModeChanged(
                AirPlaneUtils.isAirplaneOn(this),
                AirPlaneUtils.getAlertState(this)
            )
        }
    }

    open fun onAirPlaneModeChanged(isOn: Boolean, alertState: AlertState) {
        if (!isOn) {
            showShieldAlert(alertState)
        }
    }

    protected fun showShieldAlertIfNeed() {
        val alertState = AirPlaneUtils.getAlertState(this)
//        if (!AirPlaneUtils.isAirplaneOn(this)) {
//            showShieldAlert(alertState)
//        }
        showShieldAlert(alertState)
    }

    open fun processActionResult(actionResult: ActionResult) {
        if (actionResult.alertData == null) {
            if (actionResult.modalData == null) {
                processScreenData(actionResult.screenData)
            } else {
                //show modal
                processModalData(actionResult.modalData)
            }
        } else {
            //show alert
            processAlertData(actionResult.alertData)
        }
    }

    open fun processScreenData(screenData: ScreenData) {

    }

    open fun processModalData(modalData: ModalData?) {

    }

    open fun processAlertData(alertData: AlertData?) {
        if (alertData is AlertData.Shield) {
            showShieldAlert(AirPlaneUtils.getAlertState(this))
        }
    }

    protected fun showShieldAlert(alertState: AlertState?) {
        when (alertState) {
            AlertState.Active -> {
                showAlert(
                    title = "Network connected!",
                    message = "Signer detects currently connected network; please enable airplane mode, disconnect all cables and handle security breach according with your security protocol.",
                    showCancel = false,
                    confirmText = "Dismiss",
                    confirmClick = {
                        //fixme
//                        signerDataModel.pushButton(Action.GO_BACK)
                        onNetworkConnected()
                        onBack()
                    }
                )
            }
            AlertState.Past -> {
                showAlert(
                    title = "Network was connected!",
                    message = "Your Signer device has connected to a WiFi, tether or Bluetooth network since your last acknowledgement and should be considered unsafe to use. Please follow your security protocol",
                    showCancel = true,
                    cancelText = "Acknowledge and reset",
                    cancelClick = {
//                        signerDataModel.acknowledgeWarning()
//                        signerDataModel.pushButton(Action.GO_BACK)
                        onAcknowledgeAndReset()
                        onBack()
                    },
                    confirmText = "Back",
                    confirmClick = {
                        //fixme
//                        signerDataModel.pushButton(Action.GO_BACK)
                        onNetworkWasConnected()
                        onBack()
                    }
                )
            }
            AlertState.None -> {
                showAlert(
                    title = "Signer is secure",
                    showCancel = false,
                    confirmText = "Ok",
                    confirmClick = {
                        //fixme
//                        signerDataModel.pushButton(Action.GO_BACK)
                        onSecure()
                        onBack()
                    }
                )
            }
            else -> {
                showAlert(
                    title = "Network detector failure",
                    message = "Please report this error",
                    showCancel = false,
                    confirmText = "Dismiss",
                    confirmClick = {
//                        signerDataModel.pushButton(Action.GO_BACK)
                        onBack()
                    }
                )
            }
        }
    }

    open fun onNetworkConnected() {}
    open fun onNetworkWasConnected() {}
    open fun onSecure() {}

    open fun onAcknowledgeAndReset() {
        if (AirPlaneUtils.isAirplaneOn(this) && historyGetWarnings(DbUtils.dbName)) {
            historyAcknowledgeWarnings(DbUtils.dbName)
            onAirPlaneModeChanged(
                AirPlaneUtils.isAirplaneOn(this),
                AirPlaneUtils.getAlertState(this)
            )
        }
    }

    open fun onBack() {}

}