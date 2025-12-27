package com.trackmed.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation destinations for the app.
 */
sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    // Bottom navigation destinations
    data object Home : Screen(
        route = "home",
        title = "Today",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    data object Medicines : Screen(
        route = "medicines",
        title = "Medicines",
        selectedIcon = Icons.Filled.Medication,
        unselectedIcon = Icons.Outlined.Medication
    )

    data object Settings : Screen(
        route = "settings",
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    // Detail screens
    data object AddMedicine : Screen(
        route = "add_medicine",
        title = "Add Medicine"
    )

    data object EditMedicine : Screen(
        route = "edit_medicine/{medicineId}",
        title = "Edit Medicine"
    ) {
        fun createRoute(medicineId: Long) = "edit_medicine/$medicineId"
    }

    data object AddSchedule : Screen(
        route = "add_schedule/{medicineId}",
        title = "Add Schedule"
    ) {
        fun createRoute(medicineId: Long) = "add_schedule/$medicineId"
    }

    data object Onboarding : Screen(
        route = "onboarding",
        title = "Welcome"
    )

    companion object {
        val bottomNavItems = listOf(Home, Medicines, Settings)
    }
}
