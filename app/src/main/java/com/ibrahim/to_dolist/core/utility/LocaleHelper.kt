package com.ibrahim.to_dolist.core.utility

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import androidx.activity.ComponentActivity
import java.util.Locale

object LocaleHelper {

    fun setLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        // Force layout direction for RTL/LTR
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLayoutDirection(locale)
        }

        return context.createConfigurationContext(config)
    }
    // Helper function to find the activity
    fun Context.findActivity(): ComponentActivity? = when (this) {
        is ComponentActivity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
