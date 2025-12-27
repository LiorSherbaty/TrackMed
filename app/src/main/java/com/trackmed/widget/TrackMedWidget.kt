package com.trackmed.widget

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.material3.ColorProviders
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.trackmed.R
import com.trackmed.data.local.TrackMedDatabase
import com.trackmed.data.local.entity.EIntakeStatus
import com.trackmed.data.local.entity.EThemeMode
import com.trackmed.data.local.entity.ETimeWindow
import com.trackmed.ui.MainActivity
import com.trackmed.ui.theme.DarkColorScheme
import com.trackmed.ui.theme.LightColorScheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class TrackMedWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val (pendingIntakes, themeMode) = withContext(Dispatchers.IO) {
            val intakes = loadPendingIntakes(context)
            val settings = TrackMedDatabase.getInstance(context).userSettingsDao().getSettingsOnce()
            intakes to (settings?.themeMode ?: EThemeMode.SYSTEM)
        }

        val pendingCountText = context.getString(R.string.widget_pending_count, pendingIntakes.size)
        val allDoneText = context.getString(R.string.widget_all_done)

        // Determine color scheme based on theme setting
        val isSystemDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val useDarkTheme = when (themeMode) {
            EThemeMode.SYSTEM -> isSystemDark
            EThemeMode.LIGHT -> false
            EThemeMode.DARK -> true
        }

        // Create ColorProviders that forces the selected theme
        // When forcing a theme, we use the same scheme for both light and dark
        val colors = if (themeMode == EThemeMode.SYSTEM) {
            ColorProviders(light = LightColorScheme, dark = DarkColorScheme)
        } else if (useDarkTheme) {
            ColorProviders(light = DarkColorScheme, dark = DarkColorScheme)
        } else {
            ColorProviders(light = LightColorScheme, dark = LightColorScheme)
        }

        provideContent {
            GlanceTheme(colors = colors) {
                WidgetContent(pendingIntakes, pendingCountText, allDoneText)
            }
        }
    }

    private suspend fun loadPendingIntakes(context: Context): List<WidgetIntakeItem> {
        val database = TrackMedDatabase.getInstance(context)
        val today = LocalDate.now().toString()

        val pendingLogs = database.intakeLogDao().getPendingLogsForDateOnce(today)

        return pendingLogs.mapNotNull { log ->
            val medicine = database.medicineDao().getMedicineById(log.medicineId)
            if (medicine != null && medicine.isActive && !medicine.isPaused) {
                WidgetIntakeItem(
                    logId = log.id,
                    medicineId = medicine.id,
                    medicineName = medicine.name,
                    dosage = log.dosageAmount,
                    timeWindow = log.timeWindow
                )
            } else null
        }.take(5) // Limit to 5 items for widget
    }

    @Composable
    private fun WidgetContent(
        items: List<WidgetIntakeItem>,
        pendingCountText: String,
        allDoneText: String
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .padding(12.dp)
        ) {
            // Header
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .clickable(actionStartActivity<MainActivity>()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_pill),
                    contentDescription = "TrackMed",
                    modifier = GlanceModifier.size(24.dp)
                )
                Spacer(GlanceModifier.width(8.dp))
                Text(
                    text = "TrackMed",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GlanceTheme.colors.onBackground
                    )
                )
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    text = pendingCountText,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }

            Spacer(GlanceModifier.height(8.dp))

            if (items.isEmpty()) {
                // Empty state
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = allDoneText,
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                }
            } else {
                // Intake list
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(items, itemId = { it.logId }) { item ->
                        IntakeRow(item)
                        Spacer(GlanceModifier.height(4.dp))
                    }
                }
            }
        }
    }

    @Composable
    private fun IntakeRow(item: WidgetIntakeItem) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(GlanceTheme.colors.surfaceVariant)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time window icon
            Image(
                provider = ImageProvider(getTimeWindowIcon(item.timeWindow)),
                contentDescription = item.timeWindow.name,
                modifier = GlanceModifier.size(20.dp)
            )

            Spacer(GlanceModifier.width(8.dp))

            // Medicine info
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = item.medicineName,
                    style = TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = GlanceTheme.colors.onSurface
                    ),
                    maxLines = 1
                )
                if (item.dosage > 1) {
                    Text(
                        text = "x${item.dosage}",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                }
            }

            // Take button
            Box(
                modifier = GlanceModifier
                    .size(32.dp)
                    .background(GlanceTheme.colors.primary)
                    .clickable(
                        actionRunCallback<TakeActionCallback>(
                            actionParametersOf(
                                ActionParameters.Key<Long>("logId") to item.logId,
                                ActionParameters.Key<Long>("medicineId") to item.medicineId,
                                ActionParameters.Key<Int>("dosage") to item.dosage
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_check),
                    contentDescription = "Take",
                    modifier = GlanceModifier.size(16.dp)
                )
            }
        }
    }

    private fun getTimeWindowIcon(timeWindow: ETimeWindow): Int {
        return when (timeWindow) {
            ETimeWindow.MORNING -> R.drawable.ic_morning
            ETimeWindow.NOON -> R.drawable.ic_noon
            ETimeWindow.AFTERNOON -> R.drawable.ic_afternoon
            ETimeWindow.EVENING -> R.drawable.ic_evening
            ETimeWindow.NIGHT -> R.drawable.ic_night
        }
    }
}

data class WidgetIntakeItem(
    val logId: Long,
    val medicineId: Long,
    val medicineName: String,
    val dosage: Int,
    val timeWindow: ETimeWindow
)

class TakeActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val logId = parameters[ActionParameters.Key<Long>("logId")] ?: return
        val medicineId = parameters[ActionParameters.Key<Long>("medicineId")] ?: return
        val dosage = parameters[ActionParameters.Key<Int>("dosage")] ?: 1

        withContext(Dispatchers.IO) {
            val database = TrackMedDatabase.getInstance(context)

            database.intakeLogDao().updateStatus(
                logId = logId,
                status = EIntakeStatus.TAKEN,
                timestamp = System.currentTimeMillis()
            )

            database.medicineDao().decrementStock(medicineId, dosage)
        }

        // Update widget
        TrackMedWidget().update(context, glanceId)
    }
}
