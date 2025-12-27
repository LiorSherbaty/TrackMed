package com.trackmed.domain.model

import androidx.room.Embedded
import androidx.room.Relation
import com.trackmed.data.local.entity.Medicine
import com.trackmed.data.local.entity.Schedule

/**
 * A medicine with all its associated schedules.
 * Used for displaying medicine details with schedule information.
 */
data class MedicineWithSchedules(
    @Embedded
    val medicine: Medicine,

    @Relation(
        parentColumn = "id",
        entityColumn = "medicineId"
    )
    val schedules: List<Schedule>
)
