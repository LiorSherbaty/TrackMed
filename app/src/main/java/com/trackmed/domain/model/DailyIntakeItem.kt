package com.trackmed.domain.model

import com.trackmed.data.local.entity.EIntakeStatus
import com.trackmed.data.local.entity.ETimeWindow
import java.time.LocalTime

/**
 * Represents a single intake item for the daily view.
 * Combines data from IntakeLog, Medicine, and Schedule for display.
 */
data class DailyIntakeItem(
    val logId: Long,
    val medicineId: Long,
    val scheduleId: Long,
    val medicineName: String,
    val notes: String?,
    val dosageAmount: Int,
    val scheduledTime: LocalTime,
    val timeWindow: ETimeWindow,
    val status: EIntakeStatus,
    val currentStock: Int,
    val restockThreshold: Int
) {
    /** Whether the medicine stock is at or below the restock threshold */
    val isLowStock: Boolean
        get() = currentStock <= restockThreshold

    /** Whether this intake is still pending */
    val isPending: Boolean
        get() = status == EIntakeStatus.PENDING

    /** Whether this intake has been completed (taken or skipped) */
    val isCompleted: Boolean
        get() = status == EIntakeStatus.TAKEN || status == EIntakeStatus.SKIPPED
}
