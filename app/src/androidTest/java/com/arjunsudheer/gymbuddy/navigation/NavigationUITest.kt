package com.arjunsudheer.gymbuddy.navigation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import com.arjunsudheer.gymbuddy.ui.theme.GymbuddyTheme
import org.junit.Rule
import org.junit.Test

class NavigationUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun topDaySelector_showsDaysAndSelects() {
        val days = listOf("Sun", "Mon", "Tue")
        var selectedDay = "Sun"

        composeTestRule.setContent {
            GymbuddyTheme {
                TopDaySelector(
                    days = days,
                    selectedDay = selectedDay,
                    onDaySelected = { selectedDay = it }
                )
            }
        }

        composeTestRule.onNodeWithText("S").assertIsDisplayed()
        composeTestRule.onNodeWithText("M").assertIsDisplayed()
        composeTestRule.onNodeWithText("T").assertIsDisplayed()

        composeTestRule.onNodeWithText("M").performClick()
        assert(selectedDay == "Mon")
    }

    @Test
    fun bottomNavBar_displaysAllItems() {
        composeTestRule.setContent {
            GymbuddyTheme {
                val navController = rememberNavController()
                // Set a dummy graph to avoid "You must call setGraph() before calling getGraph()"
                navController.graph = navController.createGraph(startDestination = "workouts") {
                    composable("workouts") {}
                    composable("settings") {}
                }
                BottomNavBar(navController = navController)
            }
        }

        composeTestRule.onNodeWithText("Workouts").assertIsDisplayed()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
    }

    @Test
    fun bottomNavBar_itemIsClickable() {
        composeTestRule.setContent {
            GymbuddyTheme {
                val navController = rememberNavController()
                navController.graph = navController.createGraph(startDestination = "workouts") {
                    composable("workouts") {}
                    composable("settings") {}
                }
                BottomNavBar(navController = navController)
            }
        }

        composeTestRule.onNodeWithText("Workouts").performClick()
        composeTestRule.onNodeWithText("Settings").performClick()
    }
}
