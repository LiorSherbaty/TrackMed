package com.trackmed.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.trackmed.ui.screens.home.HomeScreen
import com.trackmed.ui.screens.medicines.AddEditMedicineScreen
import com.trackmed.ui.screens.medicines.MedicineListScreen
import com.trackmed.ui.screens.onboarding.OnboardingScreen
import com.trackmed.ui.screens.schedule.AddScheduleScreen
import com.trackmed.ui.screens.settings.SettingsScreen

@Composable
fun TrackMedNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Onboarding
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Home (Today view)
        composable(Screen.Home.route) {
            HomeScreen(
                onAddMedicine = {
                    navController.navigate(Screen.AddMedicine.route)
                }
            )
        }

        // Medicine List
        composable(Screen.Medicines.route) {
            MedicineListScreen(
                onAddMedicine = {
                    navController.navigate(Screen.AddMedicine.route)
                },
                onEditMedicine = { medicineId ->
                    navController.navigate(Screen.EditMedicine.createRoute(medicineId))
                }
            )
        }

        // Add Medicine
        composable(Screen.AddMedicine.route) {
            AddEditMedicineScreen(
                medicineId = null,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAddSchedule = { medicineId ->
                    // Replace AddMedicine with AddSchedule so popBackStack goes to MedicineList
                    navController.navigate(Screen.AddSchedule.createRoute(medicineId)) {
                        popUpTo(Screen.AddMedicine.route) { inclusive = true }
                    }
                }
            )
        }

        // Edit Medicine
        composable(
            route = Screen.EditMedicine.route,
            arguments = listOf(
                navArgument("medicineId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val medicineId = backStackEntry.arguments?.getLong("medicineId")
            AddEditMedicineScreen(
                medicineId = medicineId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAddSchedule = { id ->
                    navController.navigate(Screen.AddSchedule.createRoute(id))
                }
            )
        }

        // Add Schedule
        composable(
            route = Screen.AddSchedule.route,
            arguments = listOf(
                navArgument("medicineId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val medicineId = backStackEntry.arguments?.getLong("medicineId") ?: return@composable
            AddScheduleScreen(
                medicineId = medicineId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
