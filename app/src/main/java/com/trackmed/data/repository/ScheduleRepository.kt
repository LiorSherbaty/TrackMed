package com.trackmed.data.repository

import com.trackmed.data.local.dao.ScheduleDao
import com.trackmed.data.local.entity.Schedule
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepository @Inject constructor(
    private val scheduleDao: ScheduleDao
) {
    fun getSchedulesForMedicine(medicineId: Long): Flow<List<Schedule>> =
        scheduleDao.getSchedulesForMedicine(medicineId)

    suspend fun getSchedulesForMedicineOnce(medicineId: Long): List<Schedule> =
        scheduleDao.getSchedulesForMedicineOnce(medicineId)

    fun getAllActiveSchedules(): Flow<List<Schedule>> =
        scheduleDao.getAllActiveSchedules()

    suspend fun getAllSchedulesOnce(): List<Schedule> =
        scheduleDao.getAllSchedulesOnce()

    fun getSchedulesForDay(dayOfWeek: Int): Flow<List<Schedule>> =
        scheduleDao.getSchedulesForDay(dayOfWeek)

    suspend fun getActiveSchedulesForDayOnce(dayOfWeek: Int): List<Schedule> =
        scheduleDao.getActiveSchedulesForDayOnce(dayOfWeek)

    suspend fun getScheduleById(id: Long): Schedule? =
        scheduleDao.getScheduleById(id)

    suspend fun insert(schedule: Schedule): Long =
        scheduleDao.insert(schedule)

    suspend fun update(schedule: Schedule) =
        scheduleDao.update(schedule)

    suspend fun delete(schedule: Schedule) =
        scheduleDao.delete(schedule)

    suspend fun deleteById(id: Long) =
        scheduleDao.deleteById(id)

    suspend fun deleteByMedicineId(medicineId: Long) =
        scheduleDao.deleteByMedicineId(medicineId)

    suspend fun deleteAll() =
        scheduleDao.deleteAll()
}
