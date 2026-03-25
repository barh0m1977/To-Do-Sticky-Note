package com.ibrahim.to_dolist.util

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

fun Context.findFragmentActivity(): androidx.fragment.app.FragmentActivity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is androidx.fragment.app.FragmentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
class BiometricHelper(
    private val context: Context,
    private val onSuccess: () -> Unit,
    private val onError: (String) -> Unit,
) {
    fun authenticate() {
        // Use the extension to find the Activity
        val activity = context.findFragmentActivity() ?: run {
            onError("Biometric requires an Activity context")
            return
        }
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity ,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }
                override fun onAuthenticationError(code: Int, msg: CharSequence) {
                    onError(msg.toString())
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authentication Required")
            .setSubtitle("Confirm your identity to continue")
            .setNegativeButtonText("Cancel")
            .build()

        prompt.authenticate(promptInfo)
    }
}
