package com.trackmed.data.local.dao

import androidx.room.*
import com.trackmed.data.local.entity.EIntakeStatus
import com.trackmed.data.local.entity.IntakeLog
import kotlinx.coroutines.flow.Flow

@Dao
interface IntakeLogDao {

    @Query("""
        SELECT * FROM intake_logs
        WHERE scheduledDate = :date
        ORDER BY scheduledTimeHour, scheduledTimeMinute
    """)
    fun getLogsForDate(date: String): Flow<List<IntakeLog>>

    @Query("""
        SELECT * FROM intake_logs
        WHERE scheduledDate = :date
        ORDER BY scheduledTimeHour, scheduledTimeMinute
    """)
    suspend fun getLogsForDateOnce(date: String): List<IntakeLog>

    @Query("SELECT * FROM intake_logs WHERE scheduleId = :scheduleId AND scheduledDate = :date")
    suspend fun getLogForScheduleAndDate(scheduleId: Long, date: String): IntakeLog?

    @Query("SELECT * FROM intake_logs WHERE id = :id")
    suspend fun getLogById(id: Long): IntakeLog?

    @Query("""
        SELECT * FROM intake_logs
        WHERE status = 'PENDING'
        AND scheduledDate = :date
    """)
    fun getPendingLogsForDate(date: String): Flow<List<IntakeLog>>

    @Query("""
        SELECT * FROM intake_logs
        WHERE status = 'PENDING'
        AND scheduledDate = :date
    """)
    suspend fun getPendingLogsForDateOnce(date: String): List<IntakeLog>

    @Query("""
        SELECT * FROM intake_logs
        WHERE medicineId = :medicineId
        AND scheduledDate BETWEEN :startDate AND :endDate
        ORDER BY scheduledDate DESC, scheduledTimeHour DESC
    """)
    fun getLogsForMedicineInRange(
        medicineId: Long,
        startDate: String,
        endDate: String
    ): Flow<List<IntakeLog>>

    @Query("SELECT * FROM intake_logs ORDER BY id ASC")
    suspend fun getAllLogsOnce(): List<IntakeLog>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(log: IntakeLog): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(logs: List<IntakeLog>)

    @Update
    suspend fun update(log: IntakeLog)

    @Query("""
        UPDATE intake_logs
        SET status = :status, actualTimestamp = :timestamp, updatedAt = :updatedAt
        WHERE id = :logId
    """)
    suspend fun updateStatus(
        logId: Long,
        status: EIntakeStatus,
        timestamp: Long? = null,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE intake_logs
        SET followUpCount = followUpCount + 1, updatedAt = :updatedAt
        WHERE id = :logId
    """)
    suspend fun incrementFollowUpCount(
        logId: Long,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM intake_logs WHERE scheduledDate < :date")
    suspend fun deleteLogsOlderThan(date: String)

    @Query("DELETE FROM intake_logs")
    suspend fun deleteAll()
}
