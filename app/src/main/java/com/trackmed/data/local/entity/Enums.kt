package com.trackmed.data.local.entity

/**
 * Frequency type for medicine schedules.
 */
enum class EFrequencyType {
    DAILY,
    SPECIFIC_DAYS,
    INTERVAL,
    AS_NEEDED
}

/**
 * Time window for scheduling medicines.
 * Represents 5 distinct periods of the day.
 */
enum class ETimeWindow {
    MORNING,     // Early morning (e.g., 7-9 AM)
    NOON,        // Midday (e.g., 12-2 PM)
    AFTERNOON,   // Afternoon (e.g., 3-5 PM)
    EVENING,     // Evening (e.g., 6-8 PM)
    NIGHT        // Night/Bedtime (e.g., 9-11 PM)
}

/**
 * Status of an intake log entry.
 */
enum class EIntakeStatus {
    PENDING,
    TAKEN,
    SKIPPED,
    MISSED
}
