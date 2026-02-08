package com.ibrahim.to_dolist.presentation.ui.screens.settings

import com.ibrahim.to_dolist.R

enum class AppLanguage {EN,AR}
enum class AppTheme {LIGHT,DARK,SYSTEM}
enum class ExportFormat{CSV,JSON}



fun AppLanguage.labelRes(): Int = when (this) {
    AppLanguage.EN -> R.string.english
    AppLanguage.AR -> R.string.arabic
}

fun AppTheme.labelRes(): Int = when (this) {
    AppTheme.LIGHT -> R.string.light
    AppTheme.DARK -> R.string.dark
    AppTheme.SYSTEM -> R.string.system
}
