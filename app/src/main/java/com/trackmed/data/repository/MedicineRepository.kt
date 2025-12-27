package com.trackmed.data.repository

import com.trackmed.data.local.dao.MedicineDao
import com.trackmed.data.local.entity.Medicine
import com.trackmed.domain.model.MedicineWithSchedules
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicineRepository @Inject constructor(
    private val medicineDao: MedicineDao
) {
    fun getAllMedicines(): Flow<List<Medicine>> =
        medicineDao.getAllMedicines()

    suspend fun getAllMedicinesOnce(): List<Medicine> =
        medicineDao.getAllMedicinesOnce()

    fun getActiveMedicines(): Flow<List<Medicine>> =
        medicineDao.getActiveMedicines()

    suspend fun getMedicineById(id: Long): Medicine? =
        medicineDao.getMedicineById(id)

    fun getMedicineByIdFlow(id: Long): Flow<Medicine?> =
        medicineDao.getMedicineByIdFlow(id)

    fun getLowStockMedicines(): Flow<List<Medicine>> =
        medicineDao.getLowStockMedicines()

    suspend fun getLowStockMedicinesOnce(): List<Medicine> =
        medicineDao.getLowStockMedicinesOnce()

    suspend fun getMedicineWithSchedules(id: Long): MedicineWithSchedules? =
        medicineDao.getMedicineWithSchedules(id)

    fun getAllMedicinesWithSchedules(): Flow<List<MedicineWithSchedules>> =
        medicineDao.getAllMedicinesWithSchedules()

    suspend fun insert(medicine: Medicine): Long =
        medicineDao.insert(medicine)

    suspend fun update(medicine: Medicine) =
        medicineDao.update(medicine)

    suspend fun decrementStock(medicineId: Long, amount: Int) =
        medicineDao.decrementStock(medicineId, amount)

    suspend fun incrementStock(medicineId: Long, amount: Int) =
        medicineDao.incrementStock(medicineId, amount)

    suspend fun updateStock(medicineId: Long, newStock: Int) =
        medicineDao.updateStock(medicineId, newStock)

    suspend fun setPausedState(medicineId: Long, isPaused: Boolean) =
        medicineDao.setPausedState(medicineId, isPaused)

    suspend fun delete(medicine: Medicine) =
        medicineDao.delete(medicine)

    suspend fun deleteById(id: Long) =
        medicineDao.deleteById(id)

    suspend fun deleteAll() =
        medicineDao.deleteAll()
}
