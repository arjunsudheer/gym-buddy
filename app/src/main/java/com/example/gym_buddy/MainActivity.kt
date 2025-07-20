package com.example.gym_buddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.gym_buddy.bottomnavigation.BottomNavItem
import com.example.gym_buddy.bottomnavigation.BottomNavigationBar
import com.example.gym_buddy.gymsnearme.LocalGymsMap
import com.example.gym_buddy.ui.theme.GymbuddyTheme
import com.google.android.libraries.places.api.Places


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        }


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
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.GymsNearMe.route, // Default active page
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Workouts.route) { WorkoutsScreen() }
            composable(BottomNavItem.GymAI.route) { GymAI() }
            composable(BottomNavItem.GymsNearMe.route) { LocalGymsMap() }
        }
    }
}

@Composable
fun WorkoutsScreen(modifier: Modifier = Modifier) {
    Text("Workouts Screen", modifier = modifier)
}

@Composable
fun GymAI(modifier: Modifier = Modifier) {
    Text("GymAI Screen", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GymbuddyTheme {
        MainScreen()
    }
}