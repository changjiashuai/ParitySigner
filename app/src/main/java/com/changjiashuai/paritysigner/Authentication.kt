package com.changjiashuai.paritysigner

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity


class Authentication {

    fun authenticate(activity: FragmentActivity, onSuccess: () -> Unit) {
        val context = activity.applicationContext
        val biometricManager = BiometricManager.from(context)
        val hasStrongbox = hasStrongBox(context)

        Log.d("strongbox available:", hasStrongbox.toString())

        val promptInfo = buildPromptInfo(hasStrongbox)

        val authenticators = if (hasStrongbox) {
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else {
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        }

        when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {

                val executor = ContextCompat.getMainExecutor(context)

                val biometricPrompt = BiometricPrompt(
                    activity, executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence
                        ) {
                            super.onAuthenticationError(errorCode, errString)
                            Toast.makeText(
                                context,
                                "Authentication error: $errString", Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onAuthenticationSucceeded(
                            result: BiometricPrompt.AuthenticationResult
                        ) {
                            super.onAuthenticationSucceeded(result)
                            onSuccess()
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            Toast.makeText(
                                context, "Authentication failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })

                biometricPrompt.authenticate(promptInfo)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(
                    context,
                    "Insufficient security features available on this device.",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(
                    context,
                    "Security features are currently unavailable.",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(
                    context,
                    "Authentication system not enrolled; please enable "
                            + if (hasStrongbox)
                        "password or pin code"
                    else
                        "biometric authentication",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                Toast.makeText(
                    context,
                    "Security update is required",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                Toast.makeText(
                    context,
                    "Security update is required",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                Toast.makeText(
                    context,
                    "Authentication system failure",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        }

    }

    /**
     * Returns whether the device has a StrongBox backed KeyStore.
     */
    private fun hasStrongBox(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            context.packageManager
                .hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
        } else {
            false
        }
    }

    private fun buildPromptInfo(hasStrongBox: Boolean): BiometricPrompt.PromptInfo {
        return if (hasStrongBox) {
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("UNLOCK SIGNER")
                .setSubtitle("Please authenticate yourself")
                .setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build()
        } else {
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("UNLOCK SIGNER")
                .setSubtitle("Please authenticate yourself")
                .setNegativeButtonText("Cancel")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build()
        }
    }
}