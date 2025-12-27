package com.trackmed.domain.usecase

import com.trackmed.data.local.entity.EIntakeStatus
import com.trackmed.data.repository.IntakeLogRepository
import com.trackmed.data.repository.MedicineRepository
import javax.inject.Inject

/**
 * Marks an intake as taken, skipped, or missed.
 * When marked as taken, decrements the medicine stock.
 */
class MarkIntakeUseCase @Inject constructor(
    private val intakeLogRepository: IntakeLogRepository,
    private val medicineRepository: MedicineRepository
) {
    suspend operator fun invoke(
        logId: Long,
        status: EIntakeStatus
    ): Result<Unit> {
        return try {
            val log = intakeLogRepository.getLogById(logId)
                ?: return Result.failure(IllegalArgumentException("Log not found"))

            // Update log status
            intakeLogRepository.updateStatus(
                logId = logId,
                status = status,
                timestamp = if (status == EIntakeStatus.TAKEN) System.currentTimeMillis() else null
            )

            // If taken, decrement stock
            if (status == EIntakeStatus.TAKEN) {
                medicineRepository.decrementStock(log.medicineId, log.dosageAmount)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
