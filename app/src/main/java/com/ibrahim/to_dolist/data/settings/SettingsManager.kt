package com.ibrahim.to_dolist.data.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.ibrahim.to_dolist.presentation.ui.screens.settings.AppLanguage
import com.ibrahim.to_dolist.presentation.ui.screens.settings.AppTheme

class SettingsManager(private val context: Context):SettingsRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LANGUAGE = "language"
        private const val KEY_THEME = "theme"
    }

    // ---------- Language ----------

    override fun getLanguage(): AppLanguage {
        val value = prefs.getString(KEY_LANGUAGE, AppLanguage.EN.name)
        return runCatching { AppLanguage.valueOf(value!!) }
            .getOrDefault(AppLanguage.EN)
    }

    override fun setLanguage(language: AppLanguage) {
        prefs.edit { putString(KEY_LANGUAGE, language.name) }
    }

    // ---------- Theme ----------

    override fun getTheme(): AppTheme {
        val value = prefs.getString(KEY_THEME, AppTheme.SYSTEM.name)
        return runCatching { AppTheme.valueOf(value!!) }
            .getOrDefault(AppTheme.SYSTEM)
    }

    override fun setTheme(theme: AppTheme) {
        prefs.edit { putString(KEY_THEME, theme.name) }
    }

}