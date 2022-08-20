package com.changjiashuai.paritysigner

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.changjiashuai.paritysigner.ext.showAlert
import com.changjiashuai.paritysigner.models.AlertState
import com.changjiashuai.paritysigner.utils.AirPlaneUtils
import com.changjiashuai.paritysigner.utils.DbUtils
import io.parity.signer.uniffi.*

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/3 13:15.
 */
open class BaseFragment : Fragment() {

    private lateinit var airPlaneModeState: SystemActionLiveData
    private val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBackPressed()
        }
    }

    open fun onBackPressed() {}

    override fun onDetach() {
        callback.remove()
        super.onDetach()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(callback)
        context?.let { context ->
            airPlaneModeState = SystemActionLiveData(context, Intent.ACTION_AIRPLANE_MODE_CHANGED)
            airPlaneModeState.observe(this) {
                if (!AirPlaneUtils.isAirplaneOn(context)) {
                    //network is on
                    historyDeviceWasOnline(DbUtils.dbName)
                }
                onAirPlaneModeChanged(
                    AirPlaneUtils.isAirplaneOn(context),
                    AirPlaneUtils.getAlertState(context)
                )
            }
        }
    }

    open fun onAirPlaneModeChanged(isOn: Boolean, alertState: AlertState) {
        if (!isOn) {
            showShieldAlert(alertState)
        }
    }

    open fun processActionResult(actionResult: ActionResult) {
//        Log.i("BaseFragment", "actionResult=$actionResult")
        if (actionResult.alertData == null) {
            if (actionResult.modalData == null) {
                processScreenData(actionResult.screenData)
            } else {
                processModalData(actionResult.modalData!!)
            }
        } else {
            processAlertData(actionResult.alertData!!)
        }
    }

    open fun processAlertData(alertData: AlertData) {
//        Log.i("BaseFragment", "alertData=$alertData")
        when (alertData) {
            is AlertData.Shield -> {
                showShieldAlert(context?.let { AirPlaneUtils.getAlertState(it) })
            }
            is AlertData.ErrorData -> {
                Toast.makeText(context, "${alertData.f}", Toast.LENGTH_SHORT).show()
            }
            is AlertData.Confirm -> {
                Toast.makeText(context, "confirm success", Toast.LENGTH_SHORT).show()
            }
        }
    }

    open fun processModalData(modalData: ModalData) {}
    open fun processScreenData(screenData: ScreenData) {}

    protected fun showShieldAlert(alertState: AlertState?) {
        when (alertState) {
            AlertState.Active -> {
                context?.showAlert(
                    title = "Network connected!",
                    message = "Signer detects currently connected network; please enable airplane mode, disconnect all cables and handle security breach according with your security protocol.",
                    showCancel = false,
                    confirmText = "Dismiss",
                    confirmClick = {
                        onNetworkConnected()
                        onBack()
                    }
                )
            }
            AlertState.Past -> {
                context?.showAlert(
                    title = "Network was connected!",
                    message = "Your Signer device has connected to a WiFi, tether or Bluetooth network since your last acknowledgement and should be considered unsafe to use. Please follow your security protocol",
                    showCancel = true,
                    cancelText = "Acknowledge and reset",
                    cancelClick = {
                        onAcknowledgeAndReset()
                        onBack()
                    },
                    confirmText = "Back",
                    confirmClick = {
                        onNetworkWasConnected()
                        onBack()
                    }
                )
            }
            AlertState.None -> {
                context?.showAlert(
                    title = "Signer is secure",
                    showCancel = false,
                    confirmText = "Ok",
                    confirmClick = {
                        onSecure()
                        onBack()
                    }
                )
            }
            else -> {
                context?.showAlert(
                    title = "Network detector failure",
                    message = "Please report this error",
                    showCancel = false,
                    confirmText = "Dismiss",
                    confirmClick = {
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
        if (context != null) {
            if (AirPlaneUtils.isAirplaneOn(requireContext()) && historyGetWarnings(DbUtils.dbName)) {
                historyAcknowledgeWarnings(DbUtils.dbName)
                onAirPlaneModeChanged(
                    AirPlaneUtils.isAirplaneOn(requireContext()),
                    AirPlaneUtils.getAlertState(requireContext())
                )
            }
        }
    }

    open fun onBack() {}
}