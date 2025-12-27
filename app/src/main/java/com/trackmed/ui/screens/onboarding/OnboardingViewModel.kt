package com.trackmed.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackmed.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val currentPage: Int = 0,
    val notificationPermissionGranted: Boolean = false,
    val isComplete: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun nextPage() {
        _uiState.update { it.copy(currentPage = it.currentPage + 1) }
    }

    fun setNotificationPermissionGranted(granted: Boolean) {
        _uiState.update { it.copy(notificationPermissionGranted = granted) }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsRepository.markOnboardingCompleted()
            _uiState.update { it.copy(isComplete = true) }
        }
    }
}
