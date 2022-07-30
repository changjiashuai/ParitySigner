package com.changjiashuai.paritysigner.viewmodel

import android.util.Log
import com.changjiashuai.paritysigner.utils.PrefsUtils
import io.parity.signer.uniffi.Action

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/20 21:18.
 */
class NewSeedViewModel : AbsViewModel() {

    /**
     * Add seed, encrypt it, and create default accounts
     */
    fun addSeed(
        seedName: String,
        seedPhrase: String,
        createRoots: Boolean
    ) {

        // Check if seed name already exists
        if (seedNames.value?.contains(seedName) as Boolean) {
            return
        }

        // Run standard login prompt!
//        authentication.authenticate(activity) {
        try {
            // First check for seed collision
            if (PrefsUtils.sharedPreferences.all.values.contains(seedPhrase)) {
                error("This seed phrase already exists")
            }

            // Encrypt and save seed
            with(PrefsUtils.sharedPreferences.edit()) {
                putString(seedName, seedPhrase)
                apply()
            }

            refreshSeedNames()
            pushButton(
                button = Action.GO_FORWARD,
                details = if (createRoots) "true" else "false",
                seedPhrase = seedPhrase
            )
        } catch (e: Exception) {
            Log.e("Seed creation error", e.toString())
        }
//        }
    }

}