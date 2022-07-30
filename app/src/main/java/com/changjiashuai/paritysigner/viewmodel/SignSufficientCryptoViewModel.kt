package com.changjiashuai.paritysigner.viewmodel

import android.util.Log
import com.changjiashuai.paritysigner.models.SignerDataModel
import com.changjiashuai.paritysigner.models.pushButton
import com.changjiashuai.paritysigner.utils.DbUtils
import com.changjiashuai.paritysigner.utils.PrefsUtils
import io.parity.signer.models.getSeed
import io.parity.signer.uniffi.Action
import io.parity.signer.uniffi.historySeedNameWasShown

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/20 21:41.
 */
class SignSufficientCryptoViewModel : AbsViewModel() {

    //Need authentication
    fun signSufficientCrypto(seedName: String, addressKey: String) {
//        authentication.authenticate(activity) {
        val seedPhrase = getSeed(seedName)
        if (seedPhrase.isNotBlank()) {
            pushButton(
                Action.GO_FORWARD,
                addressKey,
                seedPhrase
            )
        }
//        }
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