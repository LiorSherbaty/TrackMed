package com.trackmed.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a single intake event for a schedule on a specific date.
 * Generated daily for active schedules.
 */
@Entity(
    tableName = "intake_logs",
    foreignKeys = [
        ForeignKey(
            entity = Schedule::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Medicine::class,
            parentColumns = ["id"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("scheduleId"),
        Index("medicineId"),
        Index("scheduledDate"),
        Index(value = ["scheduleId", "scheduledDate"], unique = true)
    ]
)
data class IntakeLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Reference to the schedule */
    val scheduleId: Long,

    /** Reference to the medicine (denormalized for easier querying) */
    val medicineId: Long,

    /** Scheduled date in ISO format: "2025-12-25" */
    val scheduledDate: String,

    /** Scheduled hour (0-23) */
    val scheduledTimeHour: Int,

    /** Scheduled minute (0-59) */
    val scheduledTimeMinute: Int,

    /** Time window for grouping in UI */
    val timeWindow: ETimeWindow,

    /** Dosage amount (copied from schedule for historical accuracy) */
    val dosageAmount: Int,

    /** Current status of this intake */
    val status: EIntakeStatus = EIntakeStatus.PENDING,

    /** Timestamp when marked as taken (null if not taken) */
    val actualTimestamp: Long? = null,

    /** Number of follow-up notifications sent */
    val followUpCount: Int = 0,

    /** Timestamp when created */
    val createdAt: Long = System.currentTimeMillis(),

    /** Timestamp when last updated */
    val updatedAt: Long = System.currentTimeMillis()
)
