package com.trackmed.ui.screens.medicines

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackmed.data.local.entity.Medicine
import com.trackmed.data.local.entity.Schedule
import com.trackmed.data.repository.MedicineRepository
import com.trackmed.data.repository.ScheduleRepository
import com.trackmed.domain.usecase.GenerateDailyLogsUseCase
import com.trackmed.widget.TrackMedWidgetReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AddEditMedicineUiState(
    val name: String = "",
    val notes: String = "",
    val currentStock: Int = 30,
    val restockThreshold: Int = 10,
    val pillsPerDose: Int = 1,
    val isActive: Boolean = true,
    val nameError: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val isNewMedicine: Boolean = true,
    val savedMedicineId: Long? = null,
    val existingMedicineId: Long? = null,
    val scheduleToDelete: Schedule? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AddEditMedicineViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val medicineRepository: MedicineRepository,
    private val scheduleRepository: ScheduleRepository,
    private val generateDailyLogsUseCase: GenerateDailyLogsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditMedicineUiState())
    val uiState: StateFlow<AddEditMedicineUiState> = _uiState.asStateFlow()

    private val _medicineIdFlow = MutableStateFlow<Long?>(null)

    val schedulesFlow: StateFlow<List<Schedule>> = _medicineIdFlow
        .flatMapLatest { medicineId ->
            if (medicineId != null) {
                scheduleRepository.getSchedulesForMedicine(medicineId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadMedicine(medicineId: Long) {
        _medicineIdFlow.value = medicineId

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val medicine = medicineRepository.getMedicineById(medicineId)

            if (medicine != null) {
                _uiState.update {
                    it.copy(
                        name = medicine.name,
                        notes = medicine.notes ?: "",
                        currentStock = medicine.currentStock,
                        restockThreshold = medicine.restockThreshold,
                        pillsPerDose = medicine.pillsPerDose,
                        isActive = medicine.isActive,
                        isLoading = false,
                        isNewMedicine = false,
                        existingMedicineId = medicineId
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update {
            it.copy(
                name = name,
                nameError = null
            )
        }
    }

    fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun updateCurrentStock(stock: Int) {
        _uiState.update { it.copy(currentStock = stock) }
    }

    fun updateRestockThreshold(threshold: Int) {
        _uiState.update { it.copy(restockThreshold = threshold) }
    }

    fun updatePillsPerDose(pills: Int) {
        _uiState.update { it.copy(pillsPerDose = pills) }
    }

    fun save() {
        val currentState = _uiState.value

        // Validate
        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Name is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val medicine = Medicine(
                id = currentState.existingMedicineId ?: 0,
                name = currentState.name.trim(),
                notes = currentState.notes.trim().ifBlank { null },
                currentStock = currentState.currentStock,
                restockThreshold = currentState.restockThreshold,
                pillsPerDose = currentState.pillsPerDose,
                isActive = currentState.isActive,
                updatedAt = System.currentTimeMillis()
            )

            val savedId = medicineRepository.insert(medicine)

            // Update widget to reflect changes
            TrackMedWidgetReceiver.updateAllWidgets(context)

            _uiState.update {
                it.copy(
                    isSaving = false,
                    saveSuccess = true,
                    savedMedicineId = if (currentState.isNewMedicine) savedId else null
                )
            }
        }
    }

    fun delete() {
        val medicineId = _uiState.value.existingMedicineId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            medicineRepository.deleteById(medicineId)

            // Update widget to reflect changes
            TrackMedWidgetReceiver.updateAllWidgets(context)

            _uiState.update {
                it.copy(
                    isSaving = false,
                    saveSuccess = true
                )
            }
        }
    }

    fun requestDeleteSchedule(schedule: Schedule) {
        _uiState.update { it.copy(scheduleToDelete = schedule) }
    }

    fun cancelDeleteSchedule() {
        _uiState.update { it.copy(scheduleToDelete = null) }
    }

    fun confirmDeleteSchedule() {
        val schedule = _uiState.value.scheduleToDelete ?: return

        viewModelScope.launch {
            scheduleRepository.deleteById(schedule.id)
            // Regenerate daily logs to remove the deleted schedule's entries
            generateDailyLogsUseCase(LocalDate.now())
            // Update widget to reflect changes
            TrackMedWidgetReceiver.updateAllWidgets(context)

            _uiState.update { it.copy(scheduleToDelete = null) }
        }
    }
}
