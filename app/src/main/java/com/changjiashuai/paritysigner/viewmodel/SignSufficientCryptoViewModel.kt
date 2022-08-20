package com.changjiashuai.paritysigner.viewmodel

import io.parity.signer.uniffi.Action

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/20 21:41.
 */
class SignSufficientCryptoViewModel : AbsViewModel() {

    /**
     * Sign Sufficient Crypto.
     *
     * need authenticate [com.changjiashuai.paritysigner.Authentication]
     */
    fun signSufficientCrypto(seedName: String, addressKey: String) {
        val seedPhrase = getSeed(seedName)
        if (seedPhrase.isNotBlank()) {
            doAction(
                Action.GO_FORWARD,
                addressKey,
                seedPhrase
            )
        }
    }
}