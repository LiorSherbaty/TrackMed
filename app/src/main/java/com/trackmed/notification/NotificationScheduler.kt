package com.trackmed.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.trackmed.data.local.entity.IntakeLog
import com.trackmed.data.local.entity.UserSettings
import com.trackmed.data.repository.IntakeLogRepository
import com.trackmed.data.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    suspend fun scheduleAllForDate(
        date: LocalDate,
        logs: List<IntakeLog>,
        settings: UserSettings
    ) {
        if (!settings.notificationsEnabled) return

        logs.forEach { log ->
            val triggerTime = calculateTriggerTime(date, log)
            if (triggerTime > System.currentTimeMillis()) {
                scheduleAlarm(log.id, triggerTime)
            }
        }
    }

    private fun calculateTriggerTime(date: LocalDate, log: IntakeLog): Long {
        val time = LocalTime.of(log.scheduledTimeHour, log.scheduledTimeMinute)
        return LocalDateTime.of(date, time)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    private fun scheduleAlarm(logId: Long, triggerTimeMillis: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(NotificationHelper.EXTRA_LOG_ID, logId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            logId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } else {
                // Fallback to inexact alarm when exact alarms not permitted
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
        }
    }

    fun cancelAlarm(logId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            logId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    fun scheduleFollowUp(logId: Long, delayMinutes: Int) {
        val request = OneTimeWorkRequestBuilder<FollowUpWorker>()
            .setInitialDelay(delayMinutes.toLong(), TimeUnit.MINUTES)
            .setInputData(workDataOf(NotificationHelper.EXTRA_LOG_ID to logId))
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}
