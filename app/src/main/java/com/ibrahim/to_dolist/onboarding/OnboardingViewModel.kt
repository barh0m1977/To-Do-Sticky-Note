package com.ibrahim.to_dolist.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ibrahim.to_dolist.data.onboarding.OnboardingManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class OnboardingViewModel(
    private val manager: OnboardingManager,
) : ViewModel() {

    /**
     * Emits `true` once the user has completed onboarding (persisted via DataStore).
     * Starts as `false` until the DataStore value is loaded.
     */
    val isOnboardingCompleted: StateFlow<Boolean> = manager.isOnboardingCompleted
        .stateIn(
            scope         = viewModelScope,
            started       = SharingStarted.WhileSubscribed(5_000),
            initialValue  = false,
        )

    /** Call this when the user taps "Get Started" on the last onboarding page. */
    fun completeOnboarding() {
        manager.setOnboardingCompleted()
    }
}

// ─── Factory ─────────────────────────────────────────────────────────────────

class OnboardingViewModelFactory(
    private val manager: OnboardingManager,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OnboardingViewModel(manager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}