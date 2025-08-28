package com.ibrahim.to_dolist.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SettingsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LANGUAGE = "language"
        private const val KEY_THEME = "theme"
        private const val KEY_DISPLAY_STYLE = "display_style"
    }

    fun setLanguage(language: String) {
        prefs.edit(commit = true) { putString(KEY_LANGUAGE, language) }
    }

    fun getLanguage(): String = prefs.getString(KEY_LANGUAGE, "en") ?: "en"

    fun setTheme(theme: String) {
        prefs.edit (commit = true){ putString(KEY_THEME, theme) }
    }

    fun getTheme(): String = prefs.getString(KEY_THEME, "light") ?: "light"

    fun setDisplayStyle(style: String) {
        prefs.edit(commit = true) { putString(KEY_DISPLAY_STYLE, style) }
    }

    fun getDisplayStyle(): String = prefs.getString(KEY_DISPLAY_STYLE, "display all") ?: "display all"
}
