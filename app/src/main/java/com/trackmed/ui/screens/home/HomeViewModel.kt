package com.trackmed.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackmed.data.local.entity.EIntakeStatus
import com.trackmed.data.local.entity.ETimeWindow
import com.trackmed.domain.model.DailyIntakeItem
import com.trackmed.domain.usecase.GetDailyIntakesUseCase
import com.trackmed.domain.usecase.MarkIntakeUseCase
import com.trackmed.domain.usecase.ResetIntakeUseCase
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

data class HomeUiState(
    val currentDate: LocalDate = LocalDate.now(),
    val isVacationMode: Boolean = false,
    val morningIntakes: List<DailyIntakeItem> = emptyList(),
    val noonIntakes: List<DailyIntakeItem> = emptyList(),
    val afternoonIntakes: List<DailyIntakeItem> = emptyList(),
    val eveningIntakes: List<DailyIntakeItem> = emptyList(),
    val nightIntakes: List<DailyIntakeItem> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val lowStockCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getDailyIntakesUseCase: GetDailyIntakesUseCase,
    private val markIntakeUseCase: MarkIntakeUseCase,
    private val resetIntakeUseCase: ResetIntakeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTodayIntakes()
    }

    private fun loadTodayIntakes() {
        viewModelScope.launch {
            getDailyIntakesUseCase(LocalDate.now()).collect { intakes ->
                val grouped = intakes.groupBy { it.timeWindow }

                val completedCount = intakes.count {
                    it.status == EIntakeStatus.TAKEN || it.status == EIntakeStatus.SKIPPED
                }

                val lowStockCount = intakes.count { it.isLowStock }

                _uiState.update {
                    it.copy(
                        morningIntakes = grouped[ETimeWindow.MORNING] ?: emptyList(),
                        noonIntakes = grouped[ETimeWindow.NOON] ?: emptyList(),
                        afternoonIntakes = grouped[ETimeWindow.AFTERNOON] ?: emptyList(),
                        eveningIntakes = grouped[ETimeWindow.EVENING] ?: emptyList(),
                        nightIntakes = grouped[ETimeWindow.NIGHT] ?: emptyList(),
                        completedCount = completedCount,
                        totalCount = intakes.size,
                        lowStockCount = lowStockCount,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun markAsTaken(logId: Long) {
        viewModelScope.launch {
            markIntakeUseCase(logId, EIntakeStatus.TAKEN)
            TrackMedWidgetReceiver.updateAllWidgets(context)
        }
    }

    fun markAsSkipped(logId: Long) {
        viewModelScope.launch {
            markIntakeUseCase(logId, EIntakeStatus.SKIPPED)
            TrackMedWidgetReceiver.updateAllWidgets(context)
        }
    }

    fun resetIntake(logId: Long) {
        viewModelScope.launch {
            resetIntakeUseCase(logId)
            TrackMedWidgetReceiver.updateAllWidgets(context)
        }
    }
}
