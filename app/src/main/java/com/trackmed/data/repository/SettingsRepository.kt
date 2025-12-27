package com.trackmed.data.repository

import com.trackmed.data.local.dao.UserSettingsDao
import com.trackmed.data.local.entity.EThemeMode
import com.trackmed.data.local.entity.UserSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val userSettingsDao: UserSettingsDao
) {
    fun getSettings(): Flow<UserSettings?> =
        userSettingsDao.getSettings()

    suspend fun getSettingsOnce(): UserSettings? =
        userSettingsDao.getSettingsOnce()

    suspend fun insert(settings: UserSettings) =
        userSettingsDao.insert(settings)

    suspend fun update(settings: UserSettings) =
        userSettingsDao.update(settings)

    suspend fun markOnboardingCompleted() =
        userSettingsDao.markOnboardingCompleted()

    suspend fun setVacationMode(isActive: Boolean) =
        userSettingsDao.setVacationMode(isActive)

    suspend fun setNotificationsEnabled(enabled: Boolean) =
        userSettingsDao.setNotificationsEnabled(enabled)

    suspend fun setThemeMode(themeMode: EThemeMode) =
        userSettingsDao.setThemeMode(themeMode)

    /**
     * Ensures settings exist, creating default settings if needed.
     */
    suspend fun ensureSettingsExist(): UserSettings {
        val existing = getSettingsOnce()
        if (existing != null) return existing

        val default = UserSettings()
        insert(default)
        return default
    }
}
