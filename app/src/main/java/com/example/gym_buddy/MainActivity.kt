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
import com.example.gym_buddy.gymai.GymAI
import com.example.gym_buddy.gymsnearme.LocalGymsMap
import com.example.gym_buddy.ui.theme.GymbuddyTheme
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import com.google.android.libraries.places.api.Places


class MainActivity : ComponentActivity() {
    // Initialize this once
    private lateinit var generativeModel: GenerativeModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        }

        generativeModel = GenerativeModel(
            "gemini-2.0-flash-lite",
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                temperature = 0f // Get consistent responses
                maxOutputTokens = 150 // About 100 english words
                temperature = 0.9f
                topK = 16
                topP = 0.1f
            },
            // Filter out harmful content
            safetySettings = listOf(
                SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.LOW_AND_ABOVE),
                SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.LOW_AND_ABOVE),
                SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.LOW_AND_ABOVE),
                SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.LOW_AND_ABOVE),
            )
        )


        enableEdgeToEdge()
        setContent {
            GymbuddyTheme {
                MainScreen(generativeModel)
            }
        }
    }
}

@Composable
fun MainScreen(generativeModel: GenerativeModel) {
    val navController = androidx.navigation.compose.rememberNavController()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.GymAI.route, // Default active page
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Workouts.route) { WorkoutsScreen() }
            composable(BottomNavItem.GymAI.route) { GymAI(generativeModel) }
            composable(BottomNavItem.GymsNearMe.route) { LocalGymsMap() }
        }
    }
}

@Composable
fun WorkoutsScreen(modifier: Modifier = Modifier) {
    Text("Workouts Screen", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GymbuddyTheme {
        // Create dummy generativeModel for preview sake
        MainScreen(
            generativeModel = GenerativeModel(
                "gemini-2.0-flash-lite",
                apiKey = BuildConfig.GEMINI_API_KEY,
                generationConfig = generationConfig {
                    temperature = 0f // Get consistent responses
                    maxOutputTokens = 150 // About 100 english words
                    temperature = 0.9f
                    topK = 16
                    topP = 0.1f
                },
                // Filter out harmful content
                safetySettings = listOf(
                    SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.LOW_AND_ABOVE),
                    SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.LOW_AND_ABOVE),
                    SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.LOW_AND_ABOVE),
                    SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.LOW_AND_ABOVE),
                )
            )
        )
    }
}