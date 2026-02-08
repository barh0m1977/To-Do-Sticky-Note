package com.ibrahim.to_dolist.data.settings

import com.ibrahim.to_dolist.presentation.ui.screens.settings.AppLanguage
import com.ibrahim.to_dolist.presentation.ui.screens.settings.AppTheme

interface SettingsRepository {

    fun getLanguage(): AppLanguage
    fun setLanguage(language: AppLanguage)

    fun getTheme(): AppTheme
    fun setTheme(theme: AppTheme)

}