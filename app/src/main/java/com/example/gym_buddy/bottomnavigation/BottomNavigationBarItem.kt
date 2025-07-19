package com.example.gym_buddy.bottomnavigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val title: String, val icon: ImageVector, val route: String) {
    object Workouts : BottomNavItem("Workouts", Icons.Filled.FitnessCenter, "workouts")
    object GymAI : BottomNavItem("Gym AI", Icons.Filled.Psychology, "gym_ai")
    object GymsNearMe : BottomNavItem("Gyms Near Me", Icons.Filled.LocationOn, "gyms_near_me")
}