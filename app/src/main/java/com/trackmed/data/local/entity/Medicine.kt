package com.trackmed.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a medicine or vitamin that the user tracks.
 */
@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Name of the medicine/vitamin */
    val name: String,

    /** Intake instructions: "Take with food", "Empty stomach", etc. */
    val notes: String? = null,

    /** Current stock count */
    val currentStock: Int,

    /** Threshold below which to show low stock warning */
    val restockThreshold: Int,

    /** Default pills per dose (used for stock calculation) */
    val pillsPerDose: Int = 1,

    /** Whether this medicine is active */
    val isActive: Boolean = true,

    /** Whether this medicine is paused (vacation mode) */
    val isPaused: Boolean = false,

    /** Timestamp when created */
    val createdAt: Long = System.currentTimeMillis(),

    /** Timestamp when last updated */
    val updatedAt: Long = System.currentTimeMillis()
)
