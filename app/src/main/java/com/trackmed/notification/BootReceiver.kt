package com.trackmed.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.trackmed.data.local.TrackMedDatabase
import com.trackmed.data.local.entity.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Reschedules all notifications after device reboot.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON"
        ) {
            return
        }

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = TrackMedDatabase.getInstance(context)
                val settings = database.userSettingsDao().getSettingsOnce() ?: UserSettings()

                if (!settings.notificationsEnabled || settings.vacationModeActive) {
                    return@launch
                }

                val today = LocalDate.now().toString()
                val pendingLogs = database.intakeLogDao().getPendingLogsForDateOnce(today)

                val scheduler = NotificationScheduler(context)
                scheduler.scheduleAllForDate(LocalDate.now(), pendingLogs, settings)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
