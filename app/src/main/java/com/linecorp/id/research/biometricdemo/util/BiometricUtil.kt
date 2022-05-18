package com.linecorp.id.research.biometricdemo.util

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

object BiometricUtil {
    private fun hasBiometricCapability(
        context: Context,
        authenticators: Int = BiometricManager.Authenticators.BIOMETRIC_STRONG
    ): Int {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(authenticators)
    }

    fun isBiometricReady(
        context: Context,
        authenticators: Int = BiometricManager.Authenticators.BIOMETRIC_STRONG
    ) = hasBiometricCapability(context, authenticators) == BiometricManager.BIOMETRIC_SUCCESS

    private fun createBiometricPromptInfo(
        title: String = "Biometric Authentication",
        subtitle: String = "Enter biometric credentials to proceed.",
        description: String = "Input your Fingerprint or FaceID to ensure it's you!",
        allowedAuthenticators: Int? = null
    ): BiometricPrompt.PromptInfo {
        //Create builder
        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)

        //Use Device Credentials if allowed, otherwise show Cancel Button
        if (allowedAuthenticators != null) {
            builder.setAllowedAuthenticators(allowedAuthenticators)
        } else {
            builder.setNegativeButtonText("Cancel")
        }

        return builder.build()
    }

    fun showBiometricPrompt(
        activity: AppCompatActivity,
        listener: BiometricPrompt.AuthenticationCallback,
        cryptoObject: BiometricPrompt.CryptoObject? = null
    ) {
        //Create Prompt Info
        val promptInfo = createBiometricPromptInfo()

        //Biometric Prompt
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor, listener)

        //Authenticate
        if (cryptoObject == null) {
            biometricPrompt.authenticate(promptInfo)
        } else {
            biometricPrompt.authenticate(promptInfo, cryptoObject)
        }
    }
}
