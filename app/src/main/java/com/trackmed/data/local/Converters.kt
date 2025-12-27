package com.trackmed.data.local

import androidx.room.TypeConverter
import com.trackmed.data.local.entity.EFrequencyType
import com.trackmed.data.local.entity.EIntakeStatus
import com.trackmed.data.local.entity.EThemeMode
import com.trackmed.data.local.entity.ETimeWindow

/**
 * Type converters for Room database.
 * Converts enum types to/from strings for storage.
 */
class Converters {

    @TypeConverter
    fun fromFrequencyType(value: EFrequencyType): String = value.name

    @TypeConverter
    fun toFrequencyType(value: String): EFrequencyType = EFrequencyType.valueOf(value)

    @TypeConverter
    fun fromTimeWindow(value: ETimeWindow): String = value.name

    @TypeConverter
    fun toTimeWindow(value: String): ETimeWindow = ETimeWindow.valueOf(value)

    @TypeConverter
    fun fromIntakeStatus(value: EIntakeStatus): String = value.name

    @TypeConverter
    fun toIntakeStatus(value: String): EIntakeStatus = EIntakeStatus.valueOf(value)

    @TypeConverter
    fun fromThemeMode(value: EThemeMode): String = value.name

    @TypeConverter
    fun toThemeMode(value: String): EThemeMode = EThemeMode.valueOf(value)
}
