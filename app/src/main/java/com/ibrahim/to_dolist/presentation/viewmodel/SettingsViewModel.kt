package com.ibrahim.to_dolist.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.ibrahim.to_dolist.data.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(private val settingsManager: SettingsManager):ViewModel() {
    private val _language = MutableStateFlow(settingsManager.getLanguage())
    val language: StateFlow<String> = _language

    private val _theme = MutableStateFlow(settingsManager.getTheme())
    val theme: StateFlow<String> = _theme

    private val _displayStyle = MutableStateFlow(settingsManager.getDisplayStyle())
    val displayStyle: StateFlow<String> = _displayStyle

    fun updateLanguage(lang: String) {
        settingsManager.setLanguage(lang)
        _language.value = lang

    }

    fun updateTheme(theme: String) {
        settingsManager.setTheme(theme)
        _theme.value = theme
    }

    fun updateDisplayStyle(style: String) {
        settingsManager.setDisplayStyle(style)
        _displayStyle.value = style
    }
}
