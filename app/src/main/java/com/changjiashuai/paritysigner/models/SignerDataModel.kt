package com.changjiashuai.paritysigner.models

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.changjiashuai.paritysigner.Authentication
import com.changjiashuai.paritysigner.OnBoardingState
import com.changjiashuai.paritysigner.utils.DbUtils
import com.changjiashuai.paritysigner.utils.PrefsUtils
import io.parity.signer.models.refreshSeedNames
import io.parity.signer.uniffi.*
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

/**
 * This is single object to handle all interactions with backend
 */
class SignerDataModel : ViewModel() {

    // Internal model values
    private val _onBoardingDone = MutableLiveData(OnBoardingState.InProgress)

    // TODO: something about this
    // It leaks context objects,
    // but is really quite convenient in composable things
    private lateinit var context: Context
    lateinit var activity: FragmentActivity
//    private lateinit var masterKey: MasterKey
//    private var hasStrongbox: Boolean = false

    // Alert
    private val _alertState: MutableLiveData<AlertState> = MutableLiveData(AlertState.None)

    // State of the app being unlocked
    private val _authenticated = MutableLiveData(false)

    // Authenticator to call!
    internal var authentication: Authentication = Authentication()


    // Transaction
    internal var action = JSONObject()

    // Internal storage for model data:

    // Seeds
    internal val _seedNames = MutableLiveData(arrayOf<String>())

    // Navigator
    // TODO: consider extracting components as separate livedata
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

    val seedNames: LiveData<Array<String>> = _seedNames

    val onBoardingDone: LiveData<OnBoardingState> = _onBoardingDone
    val authenticated: LiveData<Boolean> = _authenticated

    val alertState: LiveData<AlertState> = _alertState

    val actionResult: LiveData<ActionResult> = _actionResult

    val _currentAction = MutableLiveData(Action.START)
    val currentAction: LiveData<Action> = _currentAction

    // MARK: init boilerplate begin

    /**
     * Init on object creation, context not passed yet! Pass it and call next init
     */
    init {
        // actually load RustNative code
//        System.loadLibrary("signer")
    }

//    /**
//     * Don't forget to call real init after defining context!
//     */
//    fun lateInit(context: Context) {
//        this.context = context
//        //init db
////        DbUtils.initDb(context)
//
//        // Airplane mode detector
//        isAirplaneOn()
//
//        val intentFilter = IntentFilter("android.intent.action.AIRPLANE_MODE")
//
//        val receiver: BroadcastReceiver = object : BroadcastReceiver() {
//            override fun onReceive(context: Context, intent: Intent) {
//                isAirplaneOn()
//            }
//        }
//
//        context.registerReceiver(receiver, intentFilter)
//
//
//    }

    /**
     * Wipes all data
     */
    @SuppressLint("ApplySharedPref")
    fun wipe() {
        DbUtils.deleteDir(File(DbUtils.dbName))
        PrefsUtils.sharedPreferences.edit().clear().commit() // No, not apply(), do it now!
    }

    /**
     * Checks if airplane mode was off
     */
    private fun isAirplaneOn() {
        if (Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON,
                0
            ) == 0
        ) {
            if (alertState.value != AlertState.Active) {
                _alertState.value = AlertState.Active
                if (onBoardingDone.value == OnBoardingState.Yes) {
                    historyDeviceWasOnline(DbUtils.dbName)
                }
            }
        } else {
            if (alertState.value == AlertState.Active) {
                _alertState.value = if (onBoardingDone.value == OnBoardingState.Yes) {
                    AlertState.Past
                } else {
                    AlertState.None
                }
            }
        }
    }
}

/**
 * Describes current state of network detection alertness
 */
enum class AlertState {
    None,
    Active,
    Past
}
