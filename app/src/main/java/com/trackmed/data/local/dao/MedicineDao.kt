package com.trackmed.data.local.dao

import androidx.room.*
import com.trackmed.data.local.entity.Medicine
import com.trackmed.domain.model.MedicineWithSchedules
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {

    @Query("SELECT * FROM medicines ORDER BY name ASC")
    fun getAllMedicines(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines ORDER BY name ASC")
    suspend fun getAllMedicinesOnce(): List<Medicine>

    @Query("SELECT * FROM medicines WHERE isActive = 1 AND isPaused = 0 ORDER BY name ASC")
    fun getActiveMedicines(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getMedicineById(id: Long): Medicine?

    @Query("SELECT * FROM medicines WHERE id = :id")
    fun getMedicineByIdFlow(id: Long): Flow<Medicine?>

    @Query("SELECT * FROM medicines WHERE currentStock <= restockThreshold AND isActive = 1")
    fun getLowStockMedicines(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE currentStock <= restockThreshold AND isActive = 1")
    suspend fun getLowStockMedicinesOnce(): List<Medicine>

    @Transaction
    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getMedicineWithSchedules(id: Long): MedicineWithSchedules?

    @Transaction
    @Query("SELECT * FROM medicines ORDER BY name ASC")
    fun getAllMedicinesWithSchedules(): Flow<List<MedicineWithSchedules>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medicine: Medicine): Long

    @Update
    suspend fun update(medicine: Medicine)

    @Query("""
        UPDATE medicines
        SET currentStock = currentStock - :amount, updatedAt = :timestamp
        WHERE id = :medicineId AND currentStock >= :amount
    """)
    suspend fun decrementStock(
        medicineId: Long,
        amount: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE medicines
        SET currentStock = currentStock + :amount, updatedAt = :timestamp
        WHERE id = :medicineId
    """)
    suspend fun incrementStock(
        medicineId: Long,
        amount: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE medicines
        SET currentStock = :newStock, updatedAt = :timestamp
        WHERE id = :medicineId
    """)
    suspend fun updateStock(
        medicineId: Long,
        newStock: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE medicines
        SET isPaused = :isPaused, updatedAt = :timestamp
        WHERE id = :medicineId
    """)
    suspend fun setPausedState(
        medicineId: Long,
        isPaused: Boolean,
        timestamp: Long = System.currentTimeMillis()
    )

    @Delete
    suspend fun delete(medicine: Medicine)

    @Query("DELETE FROM medicines WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM medicines")
    suspend fun deleteAll()
}
