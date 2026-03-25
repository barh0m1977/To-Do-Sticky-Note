package com.ibrahim.to_dolist.core.utility

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    /**
     * Returns a locale-wrapped context for resource lookups ONLY.
     *
     * Never calls applyOverrideConfiguration — that must be done before any
     * resources are accessed (i.e. in attachBaseContext), so it is unsafe to
     * call from a Composable or anywhere inside onCreate/setContent.
     */
    fun wrap(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    fun isLeesThan(text: String): Boolean {
        return text.isNotEmpty() && text.isNotBlank() && text.length <= 13
    }
}