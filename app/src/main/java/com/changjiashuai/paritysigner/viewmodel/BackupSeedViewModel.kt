package com.changjiashuai.paritysigner.viewmodel

import android.util.Log
import com.changjiashuai.paritysigner.SeedBoxStatus
import com.changjiashuai.paritysigner.models.AlertState
import com.changjiashuai.paritysigner.utils.DbUtils
import com.changjiashuai.paritysigner.utils.PrefsUtils
import io.parity.signer.uniffi.historySeedNameWasShown

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/20 21:37.
 */
class BackupSeedViewModel : AbsViewModel() {

    //Need authentication
    fun getSeedForBackup(
        seedName: String,
        setSeedPhrase: (String) -> Unit,
        setSeedBoxStatus: (SeedBoxStatus) -> Unit
    ) {
        if (alertState.value == AlertState.None) {
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

}