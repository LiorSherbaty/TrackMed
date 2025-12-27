package com.trackmed.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * User settings - singleton table (only one row with id=1).
 */
@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey
    val id: Int = 1,

    // Notification Settings
    /** Minutes between follow-up notifications */
    val followUpIntervalMinutes: Int = 30,

    /** Maximum number of follow-up notifications before marking as missed */
    val maxFollowUps: Int = 3,

    /** Whether notifications are enabled */
    val notificationsEnabled: Boolean = true,

    // Time Windows (stored as "HH:mm")
    val morningStart: String = "07:00",
    val morningEnd: String = "09:00",
    val noonStart: String = "12:00",
    val noonEnd: String = "14:00",
    val afternoonStart: String = "15:00",
    val afternoonEnd: String = "17:00",
    val eveningStart: String = "18:00",
    val eveningEnd: String = "20:00",
    val nightStart: String = "21:00",
    val nightEnd: String = "23:00",

    // App Settings
    /** Theme mode (SYSTEM, LIGHT, DARK) */
    val themeMode: EThemeMode = EThemeMode.SYSTEM,

    /** Whether to use 24-hour time format */
    val use24HourFormat: Boolean = true,

    /** Whether vacation mode is active (pauses all notifications) */
    val vacationModeActive: Boolean = false,

    /** Whether the user has completed onboarding */
    val onboardingCompleted: Boolean = false,

    /** Timestamp when last updated */
    val updatedAt: Long = System.currentTimeMillis()
)
