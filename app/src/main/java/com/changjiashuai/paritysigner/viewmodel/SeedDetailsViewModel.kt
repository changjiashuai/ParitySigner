package com.changjiashuai.paritysigner.viewmodel

import android.content.Context
import android.util.Log
import com.changjiashuai.paritysigner.SeedBoxStatus
import com.changjiashuai.paritysigner.models.AlertState
import com.changjiashuai.paritysigner.utils.AirPlaneUtils
import com.changjiashuai.paritysigner.utils.DbUtils
import com.changjiashuai.paritysigner.utils.PrefsUtils
import io.parity.signer.uniffi.Action
import io.parity.signer.uniffi.DerivationCheck
import io.parity.signer.uniffi.historySeedNameWasShown
import io.parity.signer.uniffi.substratePathCheck

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/20 21:24.
 */
class SeedDetailsViewModel : AbsViewModel() {

    /**
     * All logic required to remove seed from memory
     *
     * 1. Remover encrypted storage item
     * 2. Synchronizes list of seeds with rust
     * 3. Calls rust remove seed logic
     */
    //Need authenticate
    fun removeSeed(seedName: String) {
//        authenticate {
        try {
            PrefsUtils.sharedPreferences.edit().remove(seedName).apply()
            refreshSeedNames()
            //fixme
            pushButton(Action.REMOVE_SEED)
        } catch (e: Exception) {
            Log.d("remove seed error", e.toString())
        }
//        }
    }


    fun checkPath(
        seedName: String,
        path: String,
        network: String
    ): DerivationCheck {
        return substratePathCheck(
            seedName = seedName,
            path = path,
            network = network,
            dbname = DbUtils.dbName
        )
    }


    //Need authentication
    fun addKey(path: String, seedName: String) {
        try {
            val seedPhrase = getSeed(seedName)
            if (seedPhrase.isNotBlank()) {
                pushButton(Action.GO_FORWARD, path, seedPhrase)
            }
        } catch (e: Exception) {
            Log.e("Add key error", e.toString())
        }
    }

    private fun getSeed(
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
        } catch (e: Exception) {
            Log.d("get seed failure", e.toString())
            ""
        }
    }

    /**
     * Need authentication
     *
     * All logic required to prepare seed box in seed backup screen
     */
    fun getSeedForBackup(
        context: Context,
        seedName: String,
        setSeedPhrase: (String) -> Unit,
        setSeedBoxStatus: (SeedBoxStatus) -> Unit
    ) {
        if (AirPlaneUtils.getAlertState(context) == AlertState.None) {
//            authentication.authenticate(activity) {
            val seedPhrase = getSeed(seedName, backup = true)
            if (seedPhrase.isBlank()) {
                setSeedPhrase("")
                setSeedBoxStatus(SeedBoxStatus.Error)
            } else {
                setSeedPhrase(seedPhrase)
                setSeedBoxStatus(SeedBoxStatus.Seed)
            }
//            }
        } else {
            setSeedPhrase("")
            setSeedBoxStatus(SeedBoxStatus.Network)
        }
    }

    fun increment(number: Int, seedName: String) {
//        authentication.authenticate(activity) {
        try {
            val seedPhrase = getSeed(seedName)
            if (seedPhrase.isNotBlank()) {
                pushButton(Action.INCREMENT, number.toString())
            }
        } catch (e: Exception) {
            Log.e("Add key error", e.toString())
        }
//        }
    }

}