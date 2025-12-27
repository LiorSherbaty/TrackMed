package com.trackmed.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.trackmed.data.local.entity.UserSettings
import com.trackmed.data.repository.IntakeLogRepository
import com.trackmed.data.repository.SettingsRepository
import com.trackmed.domain.usecase.CheckLowStockUseCase
import com.trackmed.domain.usecase.GenerateDailyLogsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@HiltWorker
class DailyLogGeneratorWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val generateDailyLogsUseCase: GenerateDailyLogsUseCase,
    private val intakeLogRepository: IntakeLogRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationScheduler: NotificationScheduler,
    private val checkLowStockUseCase: CheckLowStockUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val today = LocalDate.now()

            // Generate intake logs for today
            generateDailyLogsUseCase(today)

            // Get settings and pending logs
            val settings = settingsRepository.getSettingsOnce() ?: UserSettings()
            val pendingLogs = intakeLogRepository.getPendingLogsForDateOnce(today.toString())

            // Schedule notifications
            notificationScheduler.scheduleAllForDate(today, pendingLogs, settings)

            // Check for low stock and notify
            checkLowStockUseCase()

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        private const val WORK_NAME = "daily_log_generator"

        fun scheduleDailyWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .build()

            // Calculate delay until next midnight
            val now = LocalDateTime.now()
            val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
            val delayMillis = Duration.between(now, nextMidnight).toMillis()

            val request = PeriodicWorkRequestBuilder<DailyLogGeneratorWorker>(
                1, TimeUnit.DAYS
            )
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .addTag(WORK_NAME)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun runImmediately(context: Context) {
            val request = OneTimeWorkRequestBuilder<DailyLogGeneratorWorker>()
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
