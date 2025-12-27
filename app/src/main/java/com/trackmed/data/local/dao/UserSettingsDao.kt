package com.trackmed.data.local.dao

import androidx.room.*
import com.trackmed.data.local.entity.EThemeMode
import com.trackmed.data.local.entity.UserSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {

    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun getSettings(): Flow<UserSettings?>

    @Query("SELECT * FROM user_settings WHERE id = 1")
    suspend fun getSettingsOnce(): UserSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: UserSettings)

    @Update
    suspend fun update(settings: UserSettings)

    @Query("""
        UPDATE user_settings
        SET onboardingCompleted = 1, updatedAt = :timestamp
        WHERE id = 1
    """)
    suspend fun markOnboardingCompleted(timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE user_settings
        SET vacationModeActive = :isActive, updatedAt = :timestamp
        WHERE id = 1
    """)
    suspend fun setVacationMode(
        isActive: Boolean,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE user_settings
        SET notificationsEnabled = :enabled, updatedAt = :timestamp
        WHERE id = 1
    """)
    suspend fun setNotificationsEnabled(
        enabled: Boolean,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE user_settings
        SET themeMode = :themeMode, updatedAt = :timestamp
        WHERE id = 1
    """)
    suspend fun setThemeMode(
        themeMode: EThemeMode,
        timestamp: Long = System.currentTimeMillis()
    )
}
