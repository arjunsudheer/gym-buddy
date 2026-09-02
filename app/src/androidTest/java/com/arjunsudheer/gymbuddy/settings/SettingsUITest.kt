package com.arjunsudheer.gymbuddy.settings

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.arjunsudheer.gymbuddy.ui.theme.GymbuddyTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class SettingsUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel: SettingsViewModel = mockk(relaxed = true)
    private val weightUnitFlow = MutableStateFlow("lbs")

    @Test
    fun settingsScreen_displaysOptionsAndSelects() {
        every { viewModel.weightUnit } returns weightUnitFlow
        
        composeTestRule.setContent {
            GymbuddyTheme {
                SettingsScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("lbs").assertIsDisplayed()
        composeTestRule.onNodeWithText("kg").assertIsDisplayed()

        // Test clicking kg
        composeTestRule.onNodeWithText("kg").performClick()
        verify { viewModel.setWeightUnit("kg") }
    }
}
