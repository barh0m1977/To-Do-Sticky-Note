package com.ibrahim.to_dolist.presentation.ui.screens.settings

sealed interface SettingsEvent {
    object SendFeedback : SettingsEvent
    object OpenPrivacyPolicy : SettingsEvent
    data class ShowMessage(val message: String) : SettingsEvent

}