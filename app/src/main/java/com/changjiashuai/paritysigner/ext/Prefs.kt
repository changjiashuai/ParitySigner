package com.changjiashuai.paritysigner.ext

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/8/6 16:12.
 */
val Context.globalDataStore: DataStore<Preferences> by preferencesDataStore("settings")

//Appearance
val PREF_KEY_THEME = intPreferencesKey("theme")
val PREF_KEY_PURE_THEME = stringPreferencesKey("pureTheme")
val PREF_KEY_ACCENT = stringPreferencesKey("accent")