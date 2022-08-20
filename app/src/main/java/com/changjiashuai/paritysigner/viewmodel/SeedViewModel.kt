package com.changjiashuai.paritysigner.viewmodel

import android.content.Context
import android.util.Log
import com.changjiashuai.paritysigner.models.SeedBoxStatus
import com.changjiashuai.paritysigner.models.AlertState
import com.changjiashuai.paritysigner.utils.AirPlaneUtils
import com.changjiashuai.paritysigner.utils.PrefsUtils
import io.parity.signer.uniffi.Action

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/8/20 18:33.
 */
class SeedViewModel : AbsViewModel() {

    /**
     * Add seed, encrypt it, and create default accounts
     *
     * need authenticate [com.changjiashuai.paritysigner.Authentication]
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

        // authenticate
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
            doAction(
                button = Action.GO_FORWARD,
                details = if (createRoots) "true" else "false",
                seedPhrase = seedPhrase
            )
        } catch (e: Exception) {
            Log.e("Seed creation error", e.toString())
        }
    }

    /**
     * All logic required to remove seed from memory
     *
     * 1. Remover encrypted storage item
     * 2. Synchronizes list of seeds with rust
     * 3. Calls rust remove seed logic
     *
     *
     * need authenticate [com.changjiashuai.paritysigner.Authentication]
     */
    fun removeSeed(seedName: String) {
        try {
            PrefsUtils.sharedPreferences.edit().remove(seedName).apply()
            refreshSeedNames()
            //fixme
            doAction(Action.REMOVE_SEED)
        } catch (e: Exception) {
            Log.d("remove seed error", e.toString())
        }
    }


    /**
     * get seed for bakcup.
     *
     * need authenticate [com.changjiashuai.paritysigner.Authentication]
     */
    fun getSeedForBackup(
        context: Context,
        seedName: String,
        setSeedPhrase: (String) -> Unit,
        setSeedBoxStatus: (SeedBoxStatus) -> Unit
    ) {
        if (AirPlaneUtils.getAlertState(context) == AlertState.None) {
            val seedPhrase = getSeed(seedName, backup = true)
            if (seedPhrase.isBlank()) {
                setSeedPhrase("")
                setSeedBoxStatus(SeedBoxStatus.Error)
            } else {
                setSeedPhrase(seedPhrase)
                setSeedBoxStatus(SeedBoxStatus.Seed)
            }
        } else {
            setSeedPhrase("")
            setSeedBoxStatus(SeedBoxStatus.Network)
        }
    }
}