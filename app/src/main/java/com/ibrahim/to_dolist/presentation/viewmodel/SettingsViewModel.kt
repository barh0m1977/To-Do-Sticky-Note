package com.ibrahim.to_dolist.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.ibrahim.to_dolist.data.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsManager = SettingsManager(application)

    private val _language = MutableStateFlow(settingsManager.getLanguage())
    val language: StateFlow<String> = _language

    private val _theme = MutableStateFlow(settingsManager.getTheme())
    val theme: StateFlow<String> = _theme

    private val _dialogStyle = MutableStateFlow(settingsManager.getDialogStyle())
    val dialogStyle: StateFlow<String> = _dialogStyle

    fun updateLanguage(lang: String) {
        settingsManager.setLanguage(lang)
        _language.value = lang

    }

    fun updateTheme(theme: String) {
        settingsManager.setTheme(theme)
        _theme.value = theme
    }

    fun updateDialogStyle(style: String) {
        settingsManager.setDialogStyle(style)
        _dialogStyle.value = style
    }
}
