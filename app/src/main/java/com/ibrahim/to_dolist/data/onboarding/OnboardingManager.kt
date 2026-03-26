package com.ibrahim.to_dolist.data.onboarding

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class OnboardingManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Backed by a MutableStateFlow so it integrates cleanly with the ViewModel
    // without requiring DataStore. Reads the persisted value immediately on init.
    private val _isOnboardingCompleted = MutableStateFlow(
        prefs.getBoolean(KEY_ONBOARDING_DONE, false)
    )

    val isOnboardingCompleted: Flow<Boolean> = _isOnboardingCompleted

    fun setOnboardingCompleted() {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()
        _isOnboardingCompleted.value = true
    }

    companion object {
        private const val PREFS_NAME          = "onboarding_prefs"
        private const val KEY_ONBOARDING_DONE = "onboarding_completed"
    }
}