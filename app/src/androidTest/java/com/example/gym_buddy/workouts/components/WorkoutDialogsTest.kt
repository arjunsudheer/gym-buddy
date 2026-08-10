package com.example.gym_buddy.workouts.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.example.gym_buddy.ui.theme.GymbuddyTheme
import org.junit.Rule
import org.junit.Test

class WorkoutDialogsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun addExerciseModal_displaysLbs_whenUnitIsLbs() {
        composeTestRule.setContent {
            GymbuddyTheme {
                AddExerciseModal(
                    isRestDay = false,
                    weightUnit = "lbs",
                    onDismiss = {},
                    onSave = { _, _, _, _ -> },
                    onAddRestDay = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Weight (lbs)").assertIsDisplayed()
    }

    @Test
    fun addExerciseModal_displaysKg_whenUnitIsKg() {
        composeTestRule.setContent {
            GymbuddyTheme {
                AddExerciseModal(
                    isRestDay = false,
                    weightUnit = "kg",
                    onDismiss = {},
                    onSave = { _, _, _, _ -> },
                    onAddRestDay = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Weight (kg)").assertIsDisplayed()
    }

    @Test
    fun addExerciseModal_saveButtonDisabled_whenFieldsEmpty() {
        composeTestRule.setContent {
            GymbuddyTheme {
                AddExerciseModal(
                    isRestDay = false,
                    onDismiss = {},
                    onSave = { _, _, _, _ -> },
                    onAddRestDay = {}
                )
            }
        }

        // Initially "Weight Exercise" is selected, fields are empty
        composeTestRule.onNodeWithText("Save Exercise").assertIsNotEnabled()

        // Fill one field
        composeTestRule.onNodeWithText("Exercise Name").performTextInput("Bench Press")
        composeTestRule.onNodeWithText("Save Exercise").assertIsNotEnabled()

        // Fill all fields
        composeTestRule.onNodeWithText("Weight (lbs)").performTextInput("60")
        composeTestRule.onNodeWithText("Sets").performTextInput("3")
        composeTestRule.onNodeWithText("Reps").performTextInput("10")
        
        composeTestRule.onNodeWithText("Save Exercise").assertIsEnabled()
    }

    @Test
    fun addExerciseModal_saveButtonDisabled_whenValuesExceedMax() {
        composeTestRule.setContent {
            GymbuddyTheme {
                AddExerciseModal(
                    isRestDay = false,
                    onDismiss = {},
                    onSave = { _, _, _, _ -> },
                    onAddRestDay = {}
                )
            }
        }

        // Fill fields with valid data first
        composeTestRule.onNodeWithText("Exercise Name").performTextInput("Bench Press")
        composeTestRule.onNodeWithText("Weight (lbs)").performTextInput("60")
        composeTestRule.onNodeWithText("Sets").performTextInput("3")
        composeTestRule.onNodeWithText("Reps").performTextInput("10")
        composeTestRule.onNodeWithText("Save Exercise").assertIsEnabled()

        // Test weight > 2000
        composeTestRule.onNodeWithText("Weight (lbs)").performTextReplacement("2001")
        composeTestRule.onNodeWithText("Max weight is 2000").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save Exercise").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Weight (lbs)").performTextReplacement("60")

        // Test sets > 100
        composeTestRule.onNodeWithText("Sets").performTextReplacement("101")
        composeTestRule.onNodeWithText("Max 100").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save Exercise").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Sets").performTextReplacement("3")

        // Test reps > 100
        composeTestRule.onNodeWithText("Reps").performTextReplacement("101")
        composeTestRule.onNodeWithText("Max 100").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save Exercise").assertIsNotEnabled()
    }
}
