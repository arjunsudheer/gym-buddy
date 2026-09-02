package com.arjunsudheer.gymbuddy.workouts

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.arjunsudheer.gymbuddy.workouts.components.AddExerciseModal
import com.arjunsudheer.gymbuddy.workouts.components.WeightExerciseCard
import com.arjunsudheer.gymbuddy.workouts.models.WeightExercise
import org.junit.Rule
import org.junit.Test

class WorkoutsUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun weightExerciseCard_showsCorrectData() {
        val exercise = WeightExercise(
            id = 1,
            name = "Bench Press",
            sets = 3,
            reps = 10,
            weight = 60,
            notes = "Feeling strong"
        )

        composeTestRule.setContent {
            WeightExerciseCard(
                workout = exercise,
                isExpanded = false,
                onToggleExpand = {},
                onUpdate = {}
            )
        }

        composeTestRule.onNodeWithText("Bench Press").assertIsDisplayed()
        composeTestRule.onNodeWithText("60 • 3 sets • 10 reps").assertIsDisplayed()
    }

    @Test
    fun weightExerciseCard_expandsAndShowsFields() {
        val exercise = WeightExercise(
            id = 1,
            name = "Bench Press",
            sets = 3,
            reps = 10,
            weight = 60,
            notes = "Feeling strong"
        )

        composeTestRule.setContent {
            WeightExerciseCard(
                workout = exercise,
                isExpanded = true,
                onToggleExpand = {},
                onUpdate = {}
            )
        }

        composeTestRule.onNodeWithText("Weight").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sets").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reps").assertIsDisplayed()
        composeTestRule.onNodeWithText("Notes").assertIsDisplayed()
        
        composeTestRule.onNodeWithText("60").assertIsDisplayed()
        composeTestRule.onNodeWithText("3").assertIsDisplayed()
        composeTestRule.onNodeWithText("10").assertIsDisplayed()
        composeTestRule.onNodeWithText("Feeling strong").assertIsDisplayed()
    }

    @Test
    fun addExerciseModal_canInputData() {
        var savedName = ""
        var savedWeight = ""

        composeTestRule.setContent {
            AddExerciseModal(
                isRestDay = false,
                onDismiss = {},
                onSave = { name, weight, _, _ ->
                    savedName = name
                    savedWeight = weight
                },
                onAddRestDay = {}
            )
        }

        composeTestRule.onNodeWithText("Exercise Name").performTextInput("Deadlift")
        composeTestRule.onNodeWithText("Weight").performTextInput("100")
        
        composeTestRule.onNodeWithText("Save Exercise").performClick()
        
        assert(savedName == "Deadlift")
        assert(savedWeight == "100")
    }
}
