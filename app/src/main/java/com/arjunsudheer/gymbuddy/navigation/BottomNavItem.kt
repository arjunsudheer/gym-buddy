package com.arjunsudheer.gymbuddy.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val title: String, val icon: ImageVector, val route: String) {
    object Workouts : BottomNavItem("Workouts", Icons.Filled.FitnessCenter, "workouts")
    object Settings : BottomNavItem("Settings", Icons.Filled.Settings, "settings")
}
