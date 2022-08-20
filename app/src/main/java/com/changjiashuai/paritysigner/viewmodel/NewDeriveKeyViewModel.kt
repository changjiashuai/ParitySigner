package com.changjiashuai.paritysigner.viewmodel

import android.util.Log
import com.changjiashuai.paritysigner.utils.DbUtils
import io.parity.signer.uniffi.Action
import io.parity.signer.uniffi.DerivationCheck
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

    /**
     * Add Key.
     *
     * need authenticate [com.changjiashuai.paritysigner.Authentication]
     */
    fun addKey(path: String, seedName: String) {
        try {
            val seedPhrase = getSeed(seedName)
            if (seedPhrase.isNotBlank()) {
                doAction(Action.GO_FORWARD, path, seedPhrase)
            }
        } catch (e: Exception) {
            Log.e("Add key error", e.toString())
        }
    }
}