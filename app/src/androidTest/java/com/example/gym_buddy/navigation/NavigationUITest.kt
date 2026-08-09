package com.example.gym_buddy.navigation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.example.gym_buddy.ui.theme.GymbuddyTheme
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
                BottomNavBar(navController = navController)
            }
        }

        // Currently only "Workouts" is defined in BottomNavItem
        composeTestRule.onNodeWithText("Workouts").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Workouts").assertIsDisplayed()
    }

    @Test
    fun bottomNavBar_itemIsClickable() {
        composeTestRule.setContent {
            GymbuddyTheme {
                val navController = rememberNavController()
                BottomNavBar(navController = navController)
            }
        }

        composeTestRule.onNodeWithText("Workouts").performClick()
    }
}
