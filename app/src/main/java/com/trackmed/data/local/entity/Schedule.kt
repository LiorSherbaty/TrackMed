package com.trackmed.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a schedule for taking a medicine.
 * A medicine can have multiple schedules (e.g., morning and evening).
 */
@Entity(
    tableName = "schedules",
    foreignKeys = [
        ForeignKey(
            entity = Medicine::class,
            parentColumns = ["id"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medicineId")]
)
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Reference to the medicine */
    val medicineId: Long,

    /** Schedule frequency type */
    val frequencyType: EFrequencyType,

    /** Days of week as comma-separated values (1=Monday, 7=Sunday) for SPECIFIC_DAYS */
    val daysOfWeek: String? = null,

    /** Interval in days for INTERVAL frequency type */
    val intervalDays: Int? = null,

    /** Last date the interval schedule was taken (for calculating next date) */
    val lastIntervalDate: String? = null,

    /** Time window for this schedule */
    val timeWindow: ETimeWindow,

    /** Hour (0-23) for CUSTOM time window only */
    val customTimeHour: Int? = null,

    /** Minute (0-59) for CUSTOM time window only */
    val customTimeMinute: Int? = null,

    /** Number of pills/units to take */
    val dosageAmount: Int = 1,

    /** Whether this schedule is active */
    val isActive: Boolean = true,

    /** Timestamp when created */
    val createdAt: Long = System.currentTimeMillis()
)
