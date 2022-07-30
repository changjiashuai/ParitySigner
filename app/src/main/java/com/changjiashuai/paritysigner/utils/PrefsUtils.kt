package com.changjiashuai.paritysigner.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/18 21:56.
 */
object PrefsUtils {

    private lateinit var masterKey: MasterKey
    private const val keyStore = "AndroidKeyStore"
    lateinit var sharedPreferences: SharedPreferences

    fun initEncryptedPrefs(context: Context) {
        masterKey = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .setRequestStrongBoxBacked(true) // This might cause failures but shouldn't
                .setUserAuthenticationRequired(true)
                .build()
        } else {
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .setUserAuthenticationRequired(true)
                .build()
        }

        Log.e("ENCRY", "$context $keyStore $masterKey")

        sharedPreferences = EncryptedSharedPreferences(
            context,
            keyStore,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun isStrongBoxProtected(): Boolean {
        return if (::masterKey.isInitialized) {
            masterKey.isStrongBoxBacked
        } else {
            false
        }
    }
}