package com.trackmed.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.trackmed.data.local.entity.EIntakeStatus
import com.trackmed.data.local.entity.UserSettings
import com.trackmed.data.repository.IntakeLogRepository
import com.trackmed.data.repository.MedicineRepository
import com.trackmed.data.repository.SettingsRepository
import com.trackmed.widget.TrackMedWidgetReceiver
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class FollowUpWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val intakeLogRepository: IntakeLogRepository,
    private val medicineRepository: MedicineRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationHelper: NotificationHelper,
    private val notificationScheduler: NotificationScheduler
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val logId = inputData.getLong(NotificationHelper.EXTRA_LOG_ID, -1)
        if (logId == -1L) return Result.failure()

        val log = intakeLogRepository.getLogById(logId) ?: return Result.failure()

        // Only follow up if still pending
        if (log.status != EIntakeStatus.PENDING) {
            return Result.success()
        }

        val medicine = medicineRepository.getMedicineById(log.medicineId)
            ?: return Result.failure()

        // Check if medicine is still active
        if (!medicine.isActive || medicine.isPaused) {
            return Result.success()
        }

        val settings = settingsRepository.getSettingsOnce() ?: UserSettings()

        // Check if we've exceeded max follow-ups
        if (log.followUpCount >= settings.maxFollowUps) {
            // Mark as missed
            intakeLogRepository.updateStatus(logId, EIntakeStatus.MISSED, null)
            // Update widget to reflect the change
            TrackMedWidgetReceiver.updateAllWidgets(applicationContext)
            return Result.success()
        }

        // Increment follow-up count
        intakeLogRepository.incrementFollowUpCount(logId)
        val newFollowUpCount = log.followUpCount + 1

        // Show follow-up notification
        val notification = notificationHelper.buildIntakeReminder(
            logId = log.id,
            medicineId = medicine.id,
            medicineName = medicine.name,
            dosage = log.dosageAmount,
            notes = medicine.notes
        )
        notificationHelper.showNotification(log.id.toInt(), notification)

        // Schedule next follow-up if not at max (use the new count after increment)
        if (newFollowUpCount < settings.maxFollowUps) {
            notificationScheduler.scheduleFollowUp(logId, settings.followUpIntervalMinutes)
        }

        return Result.success()
    }
}
