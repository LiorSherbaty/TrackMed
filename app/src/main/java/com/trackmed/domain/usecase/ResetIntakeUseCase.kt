package com.trackmed.domain.usecase

import com.trackmed.data.local.entity.EIntakeStatus
import com.trackmed.data.repository.IntakeLogRepository
import com.trackmed.data.repository.MedicineRepository
import javax.inject.Inject

/**
 * Resets an intake back to pending status.
 * If the intake was previously marked as TAKEN, restores the medicine stock.
 */
class ResetIntakeUseCase @Inject constructor(
    private val intakeLogRepository: IntakeLogRepository,
    private val medicineRepository: MedicineRepository
) {
    suspend operator fun invoke(logId: Long): Result<Unit> {
        return try {
            val log = intakeLogRepository.getLogById(logId)
                ?: return Result.failure(IllegalArgumentException("Log not found"))

            // If it was taken, restore the stock
            if (log.status == EIntakeStatus.TAKEN) {
                medicineRepository.incrementStock(log.medicineId, log.dosageAmount)
            }

            // Reset status to pending
            intakeLogRepository.updateStatus(
                logId = logId,
                status = EIntakeStatus.PENDING,
                timestamp = null
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
