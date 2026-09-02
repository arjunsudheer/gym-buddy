package com.arjunsudheer.gymbuddy

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
import com.arjunsudheer.gymbuddy.navigation.BottomNavItem
import com.arjunsudheer.gymbuddy.navigation.BottomNavBar
import com.arjunsudheer.gymbuddy.settings.SettingsScreen
import com.arjunsudheer.gymbuddy.settings.SettingsViewModel
import com.arjunsudheer.gymbuddy.settings.SettingsViewModelFactory
import com.arjunsudheer.gymbuddy.ui.theme.GymbuddyTheme
import com.arjunsudheer.gymbuddy.workouts.WorkoutsScreen


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