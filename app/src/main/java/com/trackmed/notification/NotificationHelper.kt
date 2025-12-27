package com.trackmed.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.trackmed.R
import com.trackmed.data.local.entity.Medicine
import com.trackmed.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_REMINDERS = "med_reminders"
        const val CHANNEL_RESTOCK = "med_restock"
        const val CHANNEL_MISSED = "med_missed"
        const val NOTIFICATION_GROUP = "trackmed_group"

        const val ACTION_TAKE = "com.trackmed.ACTION_TAKE"
        const val ACTION_SKIP = "com.trackmed.ACTION_SKIP"
        const val ACTION_SNOOZE = "com.trackmed.ACTION_SNOOZE"

        const val EXTRA_LOG_ID = "log_id"
        const val EXTRA_MEDICINE_ID = "medicine_id"
        const val EXTRA_DOSAGE = "dosage"

        private const val RESTOCK_NOTIFICATION_ID = 999999
    }

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationChannels() {
        val reminderChannel = NotificationChannel(
            CHANNEL_REMINDERS,
            context.getString(R.string.notification_channel_reminders),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_reminders_desc)
            enableVibration(true)
            setShowBadge(true)
        }

        val restockChannel = NotificationChannel(
            CHANNEL_RESTOCK,
            context.getString(R.string.notification_channel_stock),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_stock_desc)
        }

        val missedChannel = NotificationChannel(
            CHANNEL_MISSED,
            context.getString(R.string.notification_channel_missed),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_missed_desc)
        }

        notificationManager.createNotificationChannels(
            listOf(reminderChannel, restockChannel, missedChannel)
        )
    }

    fun buildIntakeReminder(
        logId: Long,
        medicineId: Long,
        medicineName: String,
        dosage: Int,
        notes: String?
    ): Notification {
        val contentIntent = createContentIntent(logId)
        val takeIntent = createActionIntent(ACTION_TAKE, logId, medicineId, dosage)
        val skipIntent = createActionIntent(ACTION_SKIP, logId, medicineId, dosage)
        val snoozeIntent = createActionIntent(ACTION_SNOOZE, logId, medicineId, dosage)

        val dosageText = context.getString(R.string.notification_text_dosage, dosage)

        val contentText = buildString {
            append(dosageText)
            notes?.let { append("\n$it") }
        }

        return NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_title_reminder, medicineName))
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .addAction(R.drawable.ic_check, context.getString(R.string.action_take), takeIntent)
            .addAction(R.drawable.ic_close, context.getString(R.string.action_skip), skipIntent)
            .addAction(R.drawable.ic_snooze, context.getString(R.string.action_snooze), snoozeIntent)
            .setGroup(NOTIFICATION_GROUP)
            .build()
    }

    fun showNotification(notificationId: Int, notification: Notification) {
        notificationManager.notify(notificationId, notification)
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    fun showRestockAlert(medicines: List<Medicine>) {
        if (medicines.isEmpty()) return

        val title = context.getString(R.string.notification_title_stock)
        val content = medicines.joinToString(", ") { it.name }

        val notification = NotificationCompat.Builder(context, CHANNEL_RESTOCK)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(RESTOCK_NOTIFICATION_ID, notification)
    }

    private fun createContentIntent(logId: Long): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_LOG_ID, logId)
        }
        return PendingIntent.getActivity(
            context,
            logId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createActionIntent(
        action: String,
        logId: Long,
        medicineId: Long,
        dosage: Int
    ): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            this.action = action
            putExtra(EXTRA_LOG_ID, logId)
            putExtra(EXTRA_MEDICINE_ID, medicineId)
            putExtra(EXTRA_DOSAGE, dosage)
        }
        val requestCode = (logId * 10 + when (action) {
            ACTION_TAKE -> 1
            ACTION_SKIP -> 2
            else -> 3
        }).toInt()

        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
