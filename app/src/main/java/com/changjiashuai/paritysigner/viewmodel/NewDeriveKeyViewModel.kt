package com.changjiashuai.paritysigner.viewmodel

import android.util.Log
import com.changjiashuai.paritysigner.utils.DbUtils
import com.changjiashuai.paritysigner.utils.PrefsUtils
import io.parity.signer.uniffi.Action
import io.parity.signer.uniffi.DerivationCheck
import io.parity.signer.uniffi.historySeedNameWasShown
import io.parity.signer.uniffi.substratePathCheck

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/18 21:30.
 */
class NewDeriveKeyViewModel : AbsViewModel() {


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
}