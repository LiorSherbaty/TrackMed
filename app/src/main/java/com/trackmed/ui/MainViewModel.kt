package com.trackmed.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackmed.data.local.entity.EThemeMode
import com.trackmed.data.repository.SettingsRepository
import com.trackmed.domain.usecase.GenerateDailyLogsUseCase
import com.trackmed.widget.TrackMedWidgetReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class MainUiState(
    val isLoading: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val themeMode: EThemeMode = EThemeMode.SYSTEM
)

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val generateDailyLogsUseCase: GenerateDailyLogsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        observeSettings()
        generateTodaysLogs()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val settings = settingsRepository.ensureSettingsExist()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    onboardingCompleted = settings.onboardingCompleted,
                    themeMode = settings.themeMode
                )
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                settings?.let { s ->
                    _uiState.update {
                        it.copy(themeMode = s.themeMode)
                    }
                }
            }
        }
    }

    private fun generateTodaysLogs() {
        viewModelScope.launch {
            generateDailyLogsUseCase(LocalDate.now())
            // Update widget after logs are generated
            TrackMedWidgetReceiver.updateAllWidgets(context)
        }
    }
}
