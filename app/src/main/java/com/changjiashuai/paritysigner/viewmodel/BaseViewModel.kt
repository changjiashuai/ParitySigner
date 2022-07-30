package com.changjiashuai.paritysigner.viewmodel

import android.app.Activity
import android.app.Application
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.changjiashuai.paritysigner.ext.BiometricUtils.createPromptInfo
import io.parity.signer.uniffi.*

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/6/25 21:57.
 */
open class BaseViewModel(
    val applicationContext: Application
) : AndroidViewModel(applicationContext) {
    // Data storage locations
    protected var dbName: String = ""
    protected var sharedPreferences: SharedPreferences
    private val fileName = "AndroidKeyStore"
    private var masterKey: MasterKey

    var activity: FragmentActivity? = null

    init {
        dbName = applicationContext.filesDir.toString() + "/Database"

        // Init crypto for seeds:
        // https://developer.android.com/training/articles/keystore
        masterKey = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            MasterKey.Builder(applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .setRequestStrongBoxBacked(true) // This might cause failures but shouldn't
                .setUserAuthenticationRequired(true)
                .build()
        } else {
            MasterKey.Builder(applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .setUserAuthenticationRequired(true)
                .build()
        }

        sharedPreferences = EncryptedSharedPreferences(
            applicationContext,
            fileName,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

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

    /**
     * This pretty much offloads all navigation to backend!
     */
    fun pushButton(
        button: Action,
        details: String = "",
        seedPhrase: String = ""
    ) {
        try {
            _actionResult.value = backendAction(button, details, seedPhrase)
        } catch (e: java.lang.Exception) {
            Log.e("Navigation error", e.toString())
            Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    // Alert
    private val _alertState: MutableLiveData<AlertState> = MutableLiveData(AlertState.None)
    val alertState: LiveData<AlertState> = _alertState

    private fun getAlertState() {
        _alertState.value = if (historyGetWarnings(dbName)) {
            if (alertState.value == AlertState.Active) {
                AlertState.Active
            } else {
                AlertState.Past
            }
        } else {
            AlertState.None
        }
    }

    fun acknowledgeWarning() {
        if (alertState.value == AlertState.Past) {
            historyAcknowledgeWarnings(dbName)
            _alertState.value = AlertState.None
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