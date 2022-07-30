package com.changjiashuai.paritysigner.ext

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/6/26 14:29.
 */
object BiometricUtils {

    private lateinit var applicationContext: Context

    fun init(applicationContext: Context) {
        this.applicationContext = applicationContext
    }

    private fun hasStrongbox(): Boolean {
        val hasStrongbox = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            applicationContext.packageManager?.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
                ?: false
        } else {
            false
        }
        return hasStrongbox
    }

    fun createPromptInfo(): BiometricPrompt.PromptInfo {
        return if (hasStrongbox()) {
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

    fun canAuthenticate(biometricManager: BiometricManager): Int {
        val authenticatorType = if (hasStrongbox()) {
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else {
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        }
        return biometricManager.canAuthenticate(authenticatorType)
    }

    fun createBiometricPrompt(
        activity: AppCompatActivity,
        processSuccess: (BiometricPrompt.AuthenticationResult) -> Unit
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationError(errCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errCode, errString)
                Toast.makeText(
                    applicationContext,
                    "Authentication error: $errString", Toast.LENGTH_SHORT
                ).show()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(
                    applicationContext, "Authentication failed",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
//                Log.d(TAG, "Authentication was successful")
                processSuccess(result)
            }
        }
        return BiometricPrompt(activity, executor, callback)
    }

    fun authenticate(activity: FragmentActivity, onSuccess: () -> Unit) {
        val biometricManager = BiometricManager.from(applicationContext)

        val promptInfo = createPromptInfo()

        when (canAuthenticate(biometricManager)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val executor = ContextCompat.getMainExecutor(applicationContext)
                val biometricPrompt = activity?.let {
                    BiometricPrompt(
                        it, executor,
                        object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationError(
                                errorCode: Int,
                                errString: CharSequence
                            ) {
                                super.onAuthenticationError(errorCode, errString)
                                Toast.makeText(
                                    applicationContext,
                                    "Authentication error: $errString", Toast.LENGTH_SHORT
                                ).show()
                                //                                setAuth(false)
                            }

                            override fun onAuthenticationSucceeded(
                                result: BiometricPrompt.AuthenticationResult
                            ) {
                                super.onAuthenticationSucceeded(result)
                                //                                setAuth(true)
                                onSuccess()
                            }

                            override fun onAuthenticationFailed() {
                                super.onAuthenticationFailed()
                                Toast.makeText(
                                    applicationContext, "Authentication failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                                //                                setAuth(false)
                            }
                        })
                }

                biometricPrompt?.authenticate(promptInfo)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(
                    applicationContext,
                    "Insufficient security features available on this device.",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(
                    applicationContext,
                    "Security features are currently unavailable.",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(
                    applicationContext,
                    "Authentication system not enrolled; please enable "
                            + if (hasStrongbox())
                        "password or pin code"
                    else
                        "biometric authentication",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                Toast.makeText(
                    applicationContext,
                    "Security update is required",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                Toast.makeText(
                    applicationContext,
                    "Security update is required",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                Toast.makeText(
                    applicationContext,
                    "Authentication system failure",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        }
    }
}