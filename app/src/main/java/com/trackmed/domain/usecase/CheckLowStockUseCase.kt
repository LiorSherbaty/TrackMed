package com.trackmed.domain.usecase

import com.trackmed.data.repository.MedicineRepository
import com.trackmed.notification.NotificationHelper
import javax.inject.Inject

/**
 * Checks for low stock medicines and sends a notification if any are found.
 */
class CheckLowStockUseCase @Inject constructor(
    private val medicineRepository: MedicineRepository,
    private val notificationHelper: NotificationHelper
) {
    suspend operator fun invoke() {
        val lowStockMedicines = medicineRepository.getLowStockMedicinesOnce()

        if (lowStockMedicines.isNotEmpty()) {
            notificationHelper.showRestockAlert(lowStockMedicines)
        }
    }
}
