package com.example.gym_buddy.workouts.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.example.gym_buddy.ui.theme.GymbuddyTheme
import com.example.gym_buddy.workouts.models.WeightExercise
import org.junit.Rule
import org.junit.Test

class ExerciseCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun weightExerciseCard_displaysLbs_whenUnitIsLbs() {
        val exercise = WeightExercise(1, "Bench Press", 3, 10, 60, "")
        
        composeTestRule.setContent {
            GymbuddyTheme {
                WeightExerciseCard(
                    workout = exercise,
                    isExpanded = false,
                    weightUnit = "lbs",
                    onToggleExpand = {},
                    onUpdate = {}
                )
            }
        }

        // Summary text: "60 lbs • 3 sets • 10 reps"
        composeTestRule.onNodeWithText("60 lbs • 3 sets • 10 reps").assertIsDisplayed()
    }

    @Test
    fun weightExerciseCard_displaysKg_whenUnitIsKg() {
        val exercise = WeightExercise(1, "Bench Press", 3, 10, 60, "")
        
        composeTestRule.setContent {
            GymbuddyTheme {
                WeightExerciseCard(
                    workout = exercise,
                    isExpanded = false,
                    weightUnit = "kg",
                    onToggleExpand = {},
                    onUpdate = {}
                )
            }
        }

        composeTestRule.onNodeWithText("60 kg • 3 sets • 10 reps").assertIsDisplayed()
    }

    @Test
    fun weightExerciseCard_expanded_showsUnitInLabel() {
        val exercise = WeightExercise(1, "Bench Press", 3, 10, 60, "")
        
        composeTestRule.setContent {
            GymbuddyTheme {
                WeightExerciseCard(
                    workout = exercise,
                    isExpanded = true,
                    weightUnit = "kg",
                    onToggleExpand = {},
                    onUpdate = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Weight (kg)").assertIsDisplayed()
    }

    @Test
    fun weightExerciseCard_expanded_allowsEditingName() {
        val exercise = WeightExercise(1, "Bench Press", 3, 10, 60, "")
        
        composeTestRule.setContent {
            GymbuddyTheme {
                WeightExerciseCard(
                    workout = exercise,
                    isExpanded = true,
                    weightUnit = "lbs",
                    onToggleExpand = {},
                    onUpdate = {}
                )
            }
        }

        // Check if the editable name field label is present
        composeTestRule.onNodeWithText("Exercise Name").assertIsDisplayed()
        // Check if the text "Bench Press" appears in the editable field
        // We use useUnmergedTree = true if needed, or just be specific.
        // Since there are two "Bench Press" nodes, we filter for the editable one.
        composeTestRule.onAllNodesWithText("Bench Press")
            .filter(hasSetTextAction())
            .assertCountEquals(1)
    }
}
