package com.trackmed.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackmed.data.local.entity.EThemeMode
import com.trackmed.data.local.entity.UserSettings
import com.trackmed.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: EThemeMode = EThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val followUpIntervalMinutes: Int = 30,
    val maxFollowUps: Int = 3,
    val vacationModeActive: Boolean = false,
    val use24HourFormat: Boolean = true,
    val morningStart: String = "07:00",
    val morningEnd: String = "09:00",
    val noonStart: String = "12:00",
    val noonEnd: String = "14:00",
    val afternoonStart: String = "15:00",
    val afternoonEnd: String = "17:00",
    val eveningStart: String = "18:00",
    val eveningEnd: String = "20:00",
    val nightStart: String = "21:00",
    val nightEnd: String = "23:00",
    val isLoading: Boolean = true,
    val message: String? = null,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                settings?.let { s ->
                    _uiState.update {
                        it.copy(
                            themeMode = s.themeMode,
                            notificationsEnabled = s.notificationsEnabled,
                            followUpIntervalMinutes = s.followUpIntervalMinutes,
                            maxFollowUps = s.maxFollowUps,
                            vacationModeActive = s.vacationModeActive,
                            use24HourFormat = s.use24HourFormat,
                            morningStart = s.morningStart,
                            morningEnd = s.morningEnd,
                            noonStart = s.noonStart,
                            noonEnd = s.noonEnd,
                            afternoonStart = s.afternoonStart,
                            afternoonEnd = s.afternoonEnd,
                            eveningStart = s.eveningStart,
                            eveningEnd = s.eveningEnd,
                            nightStart = s.nightStart,
                            nightEnd = s.nightEnd,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun setThemeMode(themeMode: EThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(themeMode)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
        }
    }

    fun setFollowUpInterval(minutes: Int) {
        viewModelScope.launch {
            val current = settingsRepository.getSettingsOnce() ?: UserSettings()
            settingsRepository.update(
                current.copy(
                    followUpIntervalMinutes = minutes,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun setMaxFollowUps(count: Int) {
        viewModelScope.launch {
            val current = settingsRepository.getSettingsOnce() ?: UserSettings()
            settingsRepository.update(
                current.copy(
                    maxFollowUps = count,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun setVacationMode(active: Boolean) {
        viewModelScope.launch {
            settingsRepository.setVacationMode(active)
        }
    }

    fun exportBackup(context: Context) {
        // TODO: Implement with ActivityResultLauncher in the composable
        _uiState.update { it.copy(message = "Export feature coming soon") }
    }

    fun importBackup(context: Context) {
        // TODO: Implement with ActivityResultLauncher in the composable
        _uiState.update { it.copy(message = "Import feature coming soon") }
    }
}
