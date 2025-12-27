package com.trackmed.ui.screens.medicines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackmed.data.repository.MedicineRepository
import com.trackmed.domain.model.MedicineWithSchedules
import com.trackmed.domain.usecase.CalculateDaysUntilEmptyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MedicineListUiState(
    val medicines: List<MedicineWithSchedules> = emptyList(),
    val daysUntilEmpty: Map<Long, Int?> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class MedicineListViewModel @Inject constructor(
    private val medicineRepository: MedicineRepository,
    private val calculateDaysUntilEmptyUseCase: CalculateDaysUntilEmptyUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicineListUiState())
    val uiState: StateFlow<MedicineListUiState> = _uiState.asStateFlow()

    init {
        loadMedicines()
    }

    private fun loadMedicines() {
        viewModelScope.launch {
            medicineRepository.getAllMedicinesWithSchedules().collect { medicines ->
                // Calculate days until empty for each medicine
                val daysMap = mutableMapOf<Long, Int?>()
                medicines.forEach { mws ->
                    daysMap[mws.medicine.id] = calculateDaysUntilEmptyUseCase(mws.medicine)
                }

                _uiState.update {
                    it.copy(
                        medicines = medicines,
                        daysUntilEmpty = daysMap,
                        isLoading = false
                    )
                }
            }
        }
    }
}
