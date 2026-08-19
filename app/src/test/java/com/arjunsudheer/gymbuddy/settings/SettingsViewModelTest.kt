package com.arjunsudheer.gymbuddy.settings

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
    private val repository: SettingsRepository = mockk(relaxed = true)
    private val workoutDao: com.arjunsudheer.gymbuddy.workouts.WorkoutDao = mockk(relaxed = true)
    private lateinit var viewModel: SettingsViewModel
    private val weightUnitFlow = MutableStateFlow("lbs")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.weightUnit } returns weightUnitFlow
        viewModel = SettingsViewModel(repository, workoutDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial weight unit is emitted from repository`() = runTest {
        backgroundScope.launch(testDispatcher) {
            viewModel.weightUnit.collect {}
        }
        
        assertEquals("lbs", viewModel.weightUnit.value)
        
        weightUnitFlow.value = "kg"
        assertEquals("kg", viewModel.weightUnit.value)
    }

    @Test
    fun `setWeightUnit calls repository`() = runTest {
        viewModel.setWeightUnit("kg")
        coVerify { repository.setWeightUnit("kg") }
    }
}
