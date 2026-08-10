package com.example.gym_buddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.gym_buddy.navigation.BottomNavItem
import com.example.gym_buddy.navigation.BottomNavBar
import com.example.gym_buddy.settings.SettingsScreen
import com.example.gym_buddy.settings.SettingsViewModel
import com.example.gym_buddy.settings.SettingsViewModelFactory
import com.example.gym_buddy.ui.theme.GymbuddyTheme
import com.example.gym_buddy.workouts.WorkoutsScreen


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            GymbuddyTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = androidx.navigation.compose.rememberNavController()
    val context = LocalContext.current
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(context)
    )
    val weightUnit by settingsViewModel.weightUnit.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Workouts.route, // Default active page
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Workouts.route) { 
                WorkoutsScreen(weightUnit = weightUnit) 
            }
            composable(BottomNavItem.Settings.route) {
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GymbuddyTheme {
        MainScreen()
    }
}