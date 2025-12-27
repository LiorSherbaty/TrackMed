package com.trackmed.domain.usecase

import com.trackmed.data.local.entity.EIntakeStatus
import com.trackmed.data.local.entity.ETimeWindow
import com.trackmed.data.local.entity.IntakeLog
import com.trackmed.data.local.entity.Schedule
import com.trackmed.data.local.entity.UserSettings
import com.trackmed.data.repository.IntakeLogRepository
import com.trackmed.data.repository.ScheduleRepository
import com.trackmed.data.repository.SettingsRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Generates intake logs for all active schedules on a given date.
 * Should be run daily (at midnight) and on app startup.
 */
class GenerateDailyLogsUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val intakeLogRepository: IntakeLogRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(date: LocalDate) {
        val dayOfWeek = date.dayOfWeek.value // 1 = Monday, 7 = Sunday
        val dateString = date.toString()
        val settings = settingsRepository.getSettingsOnce() ?: UserSettings()

        // Skip if vacation mode is active
        if (settings.vacationModeActive) return

        val schedules = scheduleRepository.getActiveSchedulesForDayOnce(dayOfWeek)

        val logsToInsert = schedules.mapNotNull { schedule ->
            // Check if log already exists
            val existingLog = intakeLogRepository.getLogForScheduleAndDate(
                schedule.id,
                dateString
            )

            if (existingLog != null) return@mapNotNull null

            val (hour, minute) = getScheduledTime(schedule, settings)

            IntakeLog(
                scheduleId = schedule.id,
                medicineId = schedule.medicineId,
                scheduledDate = dateString,
                scheduledTimeHour = hour,
                scheduledTimeMinute = minute,
                timeWindow = schedule.timeWindow,
                dosageAmount = schedule.dosageAmount,
                status = EIntakeStatus.PENDING
            )
        }

        if (logsToInsert.isNotEmpty()) {
            intakeLogRepository.insertAll(logsToInsert)
        }
    }

    private fun getScheduledTime(schedule: Schedule, settings: UserSettings): Pair<Int, Int> {
        // Use custom time if set, otherwise use time window defaults
        if (schedule.customTimeHour != null && schedule.customTimeMinute != null) {
            return Pair(schedule.customTimeHour, schedule.customTimeMinute)
        }

        return when (schedule.timeWindow) {
            ETimeWindow.MORNING -> parseTime(settings.morningStart)
            ETimeWindow.NOON -> parseTime(settings.noonStart)
            ETimeWindow.AFTERNOON -> parseTime(settings.afternoonStart)
            ETimeWindow.EVENING -> parseTime(settings.eveningStart)
            ETimeWindow.NIGHT -> parseTime(settings.nightStart)
        }
    }

    private fun parseTime(timeString: String): Pair<Int, Int> {
        return try {
            val parts = timeString.split(":")
            if (parts.size >= 2) {
                Pair(parts[0].toInt(), parts[1].toInt())
            } else {
                Pair(8, 0) // Default fallback
            }
        } catch (e: NumberFormatException) {
            Pair(8, 0) // Default fallback for malformed time strings
        }
    }
}
