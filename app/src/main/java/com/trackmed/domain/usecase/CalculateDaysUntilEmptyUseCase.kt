package com.trackmed.domain.usecase

import com.trackmed.data.local.entity.EFrequencyType
import com.trackmed.data.local.entity.Medicine
import com.trackmed.data.repository.ScheduleRepository
import javax.inject.Inject

/**
 * Calculates how many days until a medicine runs out based on current stock and schedules.
 */
class CalculateDaysUntilEmptyUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(medicine: Medicine): Int? {
        if (medicine.currentStock <= 0) return 0

        val schedules = scheduleRepository.getSchedulesForMedicineOnce(medicine.id)
            .filter { it.isActive }

        if (schedules.isEmpty()) return null

        // Calculate weekly consumption
        val weeklyConsumption = schedules.sumOf { schedule ->
            when (schedule.frequencyType) {
                EFrequencyType.DAILY -> schedule.dosageAmount * 7
                EFrequencyType.SPECIFIC_DAYS -> {
                    // Count how many days per week this schedule runs
                    val daysCount = schedule.daysOfWeek?.split(",")?.size ?: 0
                    schedule.dosageAmount * daysCount
                }
                EFrequencyType.INTERVAL -> {
                    // Calculate average weekly doses based on interval
                    val interval = schedule.intervalDays ?: 1
                    (7.0 / interval * schedule.dosageAmount).toInt()
                }
                EFrequencyType.AS_NEEDED -> 0 // Cannot predict usage
            }
        }

        if (weeklyConsumption <= 0) return null

        val dailyConsumption = weeklyConsumption / 7.0
        return (medicine.currentStock / dailyConsumption).toInt()
    }
}
