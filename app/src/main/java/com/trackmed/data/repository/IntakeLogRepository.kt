package com.trackmed.data.repository

import com.trackmed.data.local.dao.IntakeLogDao
import com.trackmed.data.local.entity.EIntakeStatus
import com.trackmed.data.local.entity.IntakeLog
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntakeLogRepository @Inject constructor(
    private val intakeLogDao: IntakeLogDao
) {
    fun getLogsForDate(date: String): Flow<List<IntakeLog>> =
        intakeLogDao.getLogsForDate(date)

    suspend fun getLogsForDateOnce(date: String): List<IntakeLog> =
        intakeLogDao.getLogsForDateOnce(date)

    suspend fun getLogForScheduleAndDate(scheduleId: Long, date: String): IntakeLog? =
        intakeLogDao.getLogForScheduleAndDate(scheduleId, date)

    suspend fun getLogById(id: Long): IntakeLog? =
        intakeLogDao.getLogById(id)

    fun getPendingLogsForDate(date: String): Flow<List<IntakeLog>> =
        intakeLogDao.getPendingLogsForDate(date)

    suspend fun getPendingLogsForDateOnce(date: String): List<IntakeLog> =
        intakeLogDao.getPendingLogsForDateOnce(date)

    fun getLogsForMedicineInRange(
        medicineId: Long,
        startDate: String,
        endDate: String
    ): Flow<List<IntakeLog>> =
        intakeLogDao.getLogsForMedicineInRange(medicineId, startDate, endDate)

    suspend fun getAllLogsOnce(): List<IntakeLog> =
        intakeLogDao.getAllLogsOnce()

    suspend fun insert(log: IntakeLog): Long =
        intakeLogDao.insert(log)

    suspend fun insertAll(logs: List<IntakeLog>) =
        intakeLogDao.insertAll(logs)

    suspend fun update(log: IntakeLog) =
        intakeLogDao.update(log)

    suspend fun updateStatus(
        logId: Long,
        status: EIntakeStatus,
        timestamp: Long? = null
    ) = intakeLogDao.updateStatus(logId, status, timestamp)

    suspend fun incrementFollowUpCount(logId: Long) =
        intakeLogDao.incrementFollowUpCount(logId)

    suspend fun deleteLogsOlderThan(date: String) =
        intakeLogDao.deleteLogsOlderThan(date)

    suspend fun deleteAll() =
        intakeLogDao.deleteAll()
}
