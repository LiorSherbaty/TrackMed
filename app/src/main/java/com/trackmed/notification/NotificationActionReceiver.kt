package com.trackmed.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.trackmed.data.local.TrackMedDatabase
import com.trackmed.data.local.entity.EIntakeStatus
import com.trackmed.data.local.entity.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val logId = intent.getLongExtra(NotificationHelper.EXTRA_LOG_ID, -1)
        val medicineId = intent.getLongExtra(NotificationHelper.EXTRA_MEDICINE_ID, -1)
        val dosage = intent.getIntExtra(NotificationHelper.EXTRA_DOSAGE, 1)

        if (logId == -1L) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = TrackMedDatabase.getInstance(context)
                val notificationHelper = NotificationHelper(context)

                when (intent.action) {
                    NotificationHelper.ACTION_TAKE -> {
                        database.intakeLogDao().updateStatus(
                            logId = logId,
                            status = EIntakeStatus.TAKEN,
                            timestamp = System.currentTimeMillis()
                        )
                        if (medicineId != -1L) {
                            database.medicineDao().decrementStock(medicineId, dosage)
                        }
                        notificationHelper.cancelNotification(logId.toInt())
                    }

                    NotificationHelper.ACTION_SKIP -> {
                        database.intakeLogDao().updateStatus(
                            logId = logId,
                            status = EIntakeStatus.SKIPPED,
                            timestamp = null
                        )
                        notificationHelper.cancelNotification(logId.toInt())
                    }

                    NotificationHelper.ACTION_SNOOZE -> {
                        val settings = database.userSettingsDao().getSettingsOnce()
                            ?: UserSettings()
                        val scheduler = NotificationScheduler(context)
                        scheduler.scheduleFollowUp(logId, settings.followUpIntervalMinutes)
                        notificationHelper.cancelNotification(logId.toInt())
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
