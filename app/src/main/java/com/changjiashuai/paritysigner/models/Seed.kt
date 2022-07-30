package io.parity.signer.models

import android.util.Log
import android.widget.Toast
import com.changjiashuai.paritysigner.SeedBoxStatus
import com.changjiashuai.paritysigner.models.AlertState
import com.changjiashuai.paritysigner.models.SignerDataModel
import com.changjiashuai.paritysigner.models.pushButton
import com.changjiashuai.paritysigner.utils.DbUtils
import com.changjiashuai.paritysigner.utils.PrefsUtils
import io.parity.signer.uniffi.Action
import io.parity.signer.uniffi.historySeedNameWasShown
import io.parity.signer.uniffi.initNavigation
import io.parity.signer.uniffi.updateSeedNames

/**
 * Refresh seed names list
 * should be called within authentication envelope
 * authentication.authenticate(activity) {refreshSeedNames()}
 * which is somewhat asynchronous
 */
internal fun SignerDataModel.refreshSeedNames(init: Boolean = false) {
    val allNames = PrefsUtils.sharedPreferences.all.keys.sorted().toTypedArray()
    if (init) {
        initNavigation(DbUtils.dbName, allNames.toList())
    } else {
        updateSeedNames(allNames.toList())
    }
    _seedNames.value = allNames
}

/**
 * Fetch seed from strongbox; must be in unlocked scope
 */
internal fun SignerDataModel.getSeed(
    seedName: String,
    backup: Boolean = false
): String {
    return try {
        val seedPhrase = PrefsUtils.sharedPreferences.getString(seedName, "") ?: ""
        if (seedPhrase.isBlank()) {
            ""
        } else {
            if (backup) {
                historySeedNameWasShown(seedName, DbUtils.dbName)
            }
            seedPhrase
        }
    } catch (e: java.lang.Exception) {
        Log.d("get seed failure", e.toString())
        ""
    }
}

/**
 * All logic required to remove seed from memory
 *
 * 1. Remover encrypted storage item
 * 2. Synchronizes list of seeds with rust
 * 3. Calls rust remove seed logic
 */
fun SignerDataModel.removeSeed(seedName: String) {
    authentication.authenticate(activity) {
        try {
            PrefsUtils.sharedPreferences.edit().remove(seedName).apply()
            refreshSeedNames()
            pushButton(Action.REMOVE_SEED)
        } catch (e: java.lang.Exception) {
            Log.d("remove seed error", e.toString())
        }
    }
}

/**
 * All logic required to prepare seed box in seed backup screen
 */
fun SignerDataModel.getSeedForBackup(
    seedName: String,
    setSeedPhrase: (String) -> Unit,
    setSeedBoxStatus: (SeedBoxStatus) -> Unit
) {
    if (alertState.value == AlertState.None) {
        authentication.authenticate(activity) {
            val seedPhrase = getSeed(seedName, backup = true)
            if (seedPhrase.isBlank()) {
                setSeedPhrase("")
                setSeedBoxStatus(SeedBoxStatus.Error)
            } else {
                setSeedPhrase(seedPhrase)
                setSeedBoxStatus(SeedBoxStatus.Seed)
            }
        }
    } else {
        setSeedPhrase("")
        setSeedBoxStatus(SeedBoxStatus.Network)
    }
}
