package com.trackmed.data.local.dao

import androidx.room.*
import com.trackmed.data.local.entity.Schedule
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM schedules WHERE medicineId = :medicineId AND isActive = 1")
    fun getSchedulesForMedicine(medicineId: Long): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules WHERE medicineId = :medicineId AND isActive = 1")
    suspend fun getSchedulesForMedicineOnce(medicineId: Long): List<Schedule>

    @Query("SELECT * FROM schedules WHERE isActive = 1")
    fun getAllActiveSchedules(): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules ORDER BY id ASC")
    suspend fun getAllSchedulesOnce(): List<Schedule>

    @Query("""
        SELECT s.* FROM schedules s
        INNER JOIN medicines m ON s.medicineId = m.id
        WHERE s.isActive = 1
        AND m.isActive = 1
        AND m.isPaused = 0
        AND (
            s.frequencyType = 'DAILY'
            OR (s.frequencyType = 'SPECIFIC_DAYS' AND s.daysOfWeek LIKE '%' || :dayOfWeek || '%')
            OR s.frequencyType = 'INTERVAL'
        )
        ORDER BY
            CASE s.timeWindow
                WHEN 'MORNING' THEN 1
                WHEN 'NOON' THEN 2
                WHEN 'AFTERNOON' THEN 3
                WHEN 'EVENING' THEN 4
                WHEN 'NIGHT' THEN 5
            END,
            s.customTimeHour,
            s.customTimeMinute
    """)
    fun getSchedulesForDay(dayOfWeek: Int): Flow<List<Schedule>>

    @Query("""
        SELECT s.* FROM schedules s
        INNER JOIN medicines m ON s.medicineId = m.id
        WHERE s.isActive = 1
        AND m.isActive = 1
        AND m.isPaused = 0
        AND (
            s.frequencyType = 'DAILY'
            OR (s.frequencyType = 'SPECIFIC_DAYS' AND s.daysOfWeek LIKE '%' || :dayOfWeek || '%')
            OR s.frequencyType = 'INTERVAL'
        )
    """)
    suspend fun getActiveSchedulesForDayOnce(dayOfWeek: Int): List<Schedule>

    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getScheduleById(id: Long): Schedule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: Schedule): Long

    @Update
    suspend fun update(schedule: Schedule)

    @Delete
    suspend fun delete(schedule: Schedule)

    @Query("DELETE FROM schedules WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM schedules WHERE medicineId = :medicineId")
    suspend fun deleteByMedicineId(medicineId: Long)

    @Query("DELETE FROM schedules")
    suspend fun deleteAll()
}
