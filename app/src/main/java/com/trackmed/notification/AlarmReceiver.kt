package com.trackmed.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.trackmed.data.local.TrackMedDatabase
import com.trackmed.data.local.entity.EIntakeStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val logId = intent.getLongExtra(NotificationHelper.EXTRA_LOG_ID, -1)
        if (logId == -1L) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = TrackMedDatabase.getInstance(context)
                val log = database.intakeLogDao().getLogById(logId)

                if (log != null && log.status == EIntakeStatus.PENDING) {
                    val medicine = database.medicineDao().getMedicineById(log.medicineId)

                    if (medicine != null && medicine.isActive && !medicine.isPaused) {
                        val notificationHelper = NotificationHelper(context)

                        val notification = notificationHelper.buildIntakeReminder(
                            logId = log.id,
                            medicineId = medicine.id,
                            medicineName = medicine.name,
                            dosage = log.dosageAmount,
                            notes = medicine.notes
                        )

                        notificationHelper.showNotification(log.id.toInt(), notification)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
