package com.ibrahim.to_dolist.util

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricHelper(
    private val context: Context,
    private val onSuccess: () -> Unit,
    private val onError: (String) -> Unit
) {
    fun authenticate() {

        val activity = context as? FragmentActivity ?:run{
        onError("Biometric requires FragmentActivity")
            return
        }
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(code: Int, msg: CharSequence) {
                    onError("Error: $msg")
                }

                override fun onAuthenticationFailed() {
                    onError("Fingerprint not recognized")
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Confirm Fingerprint")
            .setSubtitle("Use fingerprint to continue")
            .setNegativeButtonText("Cancel")
            .build()
        prompt.authenticate(promptInfo)


    }
}
