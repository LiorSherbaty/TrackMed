package com.trackmed.domain.usecase

import com.trackmed.data.local.entity.IntakeLog
import com.trackmed.data.local.entity.Medicine
import com.trackmed.data.repository.IntakeLogRepository
import com.trackmed.data.repository.MedicineRepository
import com.trackmed.domain.model.DailyIntakeItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/**
 * Gets all intake items for a specific date, combining log and medicine data.
 */
class GetDailyIntakesUseCase @Inject constructor(
    private val intakeLogRepository: IntakeLogRepository,
    private val medicineRepository: MedicineRepository
) {
    operator fun invoke(date: LocalDate): Flow<List<DailyIntakeItem>> {
        val dateString = date.toString()

        return combine(
            intakeLogRepository.getLogsForDate(dateString),
            medicineRepository.getAllMedicines()
        ) { logs, medicines ->
            val medicineMap = medicines.associateBy { it.id }
            logs.mapNotNull { log ->
                medicineMap[log.medicineId]?.let { medicine ->
                    log.toDailyIntakeItem(medicine)
                }
            }
        }
    }

    private fun IntakeLog.toDailyIntakeItem(medicine: Medicine): DailyIntakeItem {
        return DailyIntakeItem(
            logId = id,
            medicineId = medicineId,
            scheduleId = scheduleId,
            medicineName = medicine.name,
            notes = medicine.notes,
            dosageAmount = dosageAmount,
            scheduledTime = LocalTime.of(scheduledTimeHour, scheduledTimeMinute),
            timeWindow = timeWindow,
            status = status,
            currentStock = medicine.currentStock,
            restockThreshold = medicine.restockThreshold
        )
    }
}
