package com.trackmed.domain.model

import com.trackmed.data.local.entity.ETimeWindow
import java.time.LocalTime

/**
 * Configuration for a time window, including display information.
 */
data class TimeWindowConfig(
    val window: ETimeWindow,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val displayName: String
)
