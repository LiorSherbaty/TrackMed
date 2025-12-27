package com.trackmed.ui.screens.schedule

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackmed.data.local.entity.EFrequencyType
import com.trackmed.data.local.entity.ETimeWindow
import com.trackmed.data.local.entity.Schedule
import com.trackmed.data.repository.ScheduleRepository
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

data class AddScheduleUiState(
    val medicineId: Long = 0,
    val frequencyType: EFrequencyType = EFrequencyType.DAILY,
    val selectedDays: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7), // All days selected by default
    val intervalDays: Int = 2,
    val timeWindow: ETimeWindow = ETimeWindow.MORNING,
    val customTimeHour: Int = 8,
    val customTimeMinute: Int = 0,
    val dosageAmount: Int = 1,
    val isSaving: Boolean = false,
    val showSaveSuccessDialog: Boolean = false,
    val navigateBack: Boolean = false
)

@HiltViewModel
class AddScheduleViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scheduleRepository: ScheduleRepository,
    private val generateDailyLogsUseCase: GenerateDailyLogsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddScheduleUiState())
    val uiState: StateFlow<AddScheduleUiState> = _uiState.asStateFlow()

    fun setMedicineId(medicineId: Long) {
        _uiState.update { it.copy(medicineId = medicineId) }
    }

    fun updateFrequencyType(type: EFrequencyType) {
        _uiState.update { it.copy(frequencyType = type) }
    }

    fun toggleDay(day: Int) {
        _uiState.update { state ->
            val newDays = if (state.selectedDays.contains(day)) {
                state.selectedDays - day
            } else {
                state.selectedDays + day
            }
            state.copy(selectedDays = newDays)
        }
    }

    fun updateIntervalDays(days: Int) {
        _uiState.update { it.copy(intervalDays = days.coerceIn(1, 30)) }
    }

    fun updateTimeWindow(window: ETimeWindow) {
        _uiState.update { it.copy(timeWindow = window) }
    }

    fun updateCustomTime(hour: Int, minute: Int) {
        _uiState.update {
            it.copy(
                customTimeHour = hour,
                customTimeMinute = minute
            )
        }
    }

    fun updateDosageAmount(amount: Int) {
        _uiState.update { it.copy(dosageAmount = amount) }
    }

    fun save() {
        val currentState = _uiState.value

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val schedule = Schedule(
                medicineId = currentState.medicineId,
                frequencyType = currentState.frequencyType,
                daysOfWeek = if (currentState.frequencyType == EFrequencyType.SPECIFIC_DAYS) {
                    currentState.selectedDays.sorted().joinToString(",")
                } else null,
                intervalDays = if (currentState.frequencyType == EFrequencyType.INTERVAL) {
                    currentState.intervalDays
                } else null,
                timeWindow = currentState.timeWindow,
                customTimeHour = currentState.customTimeHour,
                customTimeMinute = currentState.customTimeMinute,
                dosageAmount = currentState.dosageAmount
            )

            scheduleRepository.insert(schedule)

            // Generate intake logs for today so the new schedule appears immediately
            generateDailyLogsUseCase(LocalDate.now())

            // Update widget to show new intake
            TrackMedWidgetReceiver.updateAllWidgets(context)

            _uiState.update {
                it.copy(
                    isSaving = false,
                    showSaveSuccessDialog = true
                )
            }
        }
    }

    fun onAddAnotherSchedule() {
        val medicineId = _uiState.value.medicineId
        _uiState.update {
            AddScheduleUiState(medicineId = medicineId)
        }
    }

    fun onDone() {
        _uiState.update { it.copy(navigateBack = true) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(showSaveSuccessDialog = false) }
    }
}
