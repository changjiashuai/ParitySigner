package com.changjiashuai.paritysigner.utils

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.TypedValue
import androidx.appcompat.app.AppCompatDelegate
import com.changjiashuai.paritysigner.R
import com.google.android.material.color.DynamicColors

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/8/6 16:42.
 */
object ThemeHelper {

    fun updateTheme(activity: Activity) {
//        val themeMode = PreferenceHelper.getString(PreferenceKeys.THEME_MODE, "A")!!
//        val pureThemeEnabled = PreferenceHelper.getBoolean(PreferenceKeys.PURE_THEME, false)
//
//        updateAccentColor(activity, pureThemeEnabled)
//        updateThemeMode(themeMode)
    }

    private fun updateAccentColor(
        activity: Activity,
        pureThemeEnabled: Boolean
    ) {
//        val theme = when (
//            PreferenceHelper.getString(
//                PreferenceKeys.ACCENT_COLOR,
//                "purple"
//            )
//        ) {
//            "my" -> {
//                applyDynamicColors(activity)
//                if (pureThemeEnabled) R.style.MaterialYou_Pure
//                else R.style.MaterialYou
//            }
//            // set the theme, use the pure theme if enabled
//            "red" -> if (pureThemeEnabled) R.style.Theme_Red_Pure else R.style.Theme_Red
//            "blue" -> if (pureThemeEnabled) R.style.Theme_Blue_Pure else R.style.Theme_Blue
//            "yellow" -> if (pureThemeEnabled) R.style.Theme_Yellow_Pure else R.style.Theme_Yellow
//            "green" -> if (pureThemeEnabled) R.style.Theme_Green_Pure else R.style.Theme_Green
//            "purple" -> if (pureThemeEnabled) R.style.Theme_Purple_Pure else R.style.Theme_Purple
//            else -> if (pureThemeEnabled) R.style.Theme_Purple_Pure else R.style.Theme_Purple
//        }
//        activity.setTheme(theme)
    }

    private fun applyDynamicColors(activity: Activity) {
        /**
         * apply dynamic colors to the activity
         */
        DynamicColors.applyToActivityIfAvailable(activity)
    }

    private fun updateThemeMode(themeMode: String) {
        val mode = when (themeMode) {
            "A" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            "L" -> AppCompatDelegate.MODE_NIGHT_NO
            "D" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun updateNightMode(nightMode: Int) {
        val mode = when (nightMode) {
            0 -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            1 -> AppCompatDelegate.MODE_NIGHT_NO
            2 -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun getThemeColor(context: Context, colorCode: Int): Int {
        val value = TypedValue()
        context.theme.resolveAttribute(colorCode, value, true)
        return value.data
    }

    // Needed due to different MainActivity Aliases because of the app icons
    fun restartMainActivity(context: Context) {
        // kill player notification
        val nManager = context
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nManager.cancelAll()
        // start a new Intent of the app
        val pm: PackageManager = context.packageManager
        val intent = pm.getLaunchIntentForPackage(context.packageName)
        intent?.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
        // kill the old application
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}