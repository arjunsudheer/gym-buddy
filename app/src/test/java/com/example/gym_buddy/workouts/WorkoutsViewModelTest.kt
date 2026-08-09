package com.example.gym_buddy.workouts

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.gym_buddy.workouts.models.RestExercise
import com.example.gym_buddy.workouts.models.WeightExercise
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutsViewModelTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: WorkoutsViewModel
    private val application: Application = mockk(relaxed = true)
    private val workoutDao: WorkoutDao = mockk(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock DAO returns for all days
        listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday").forEach { day ->
            every { workoutDao.getWorkoutsForDay(day) } returns flowOf(emptyList())
        }
        
        viewModel = WorkoutsViewModel(application, workoutDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() = runTest {
        val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        assertEquals(days, viewModel.daysOfWeek)
        
        // selectedDay depends on current time, so we just check it's one of the days
        assertTrue(days.contains(viewModel.selectedDay.value))
        
        assertNull(viewModel.expandedWorkoutId.value)
        assertEquals(false, viewModel.isAddModalVisible.value)
        assertNull(viewModel.workoutToDelete.value)
        assertEquals(false, viewModel.showRestDayConfirmation.value)
    }

    @Test
    fun `selectDay updates selectedDay`() = runTest {
        viewModel.selectDay("Mon")
        assertEquals("Mon", viewModel.selectedDay.value)
        
        viewModel.selectDay("Fri")
        assertEquals("Fri", viewModel.selectedDay.value)
    }

    @Test
    fun `toggleWorkoutExpansion updates expandedWorkoutId`() = runTest {
        viewModel.toggleWorkoutExpansion(1)
        assertEquals(1, viewModel.expandedWorkoutId.value)
        
        viewModel.toggleWorkoutExpansion(1)
        assertNull(viewModel.expandedWorkoutId.value)
        
        viewModel.toggleWorkoutExpansion(2)
        assertEquals(2, viewModel.expandedWorkoutId.value)
    }

    @Test
    fun `showAddModal updates isAddModalVisible`() = runTest {
        viewModel.showAddModal(true)
        assertEquals(true, viewModel.isAddModalVisible.value)
        
        viewModel.showAddModal(false)
        assertEquals(false, viewModel.isAddModalVisible.value)
    }

    @Test
    fun `confirmDeleteWorkout updates workoutToDelete`() = runTest {
        val exercise = WeightExercise(1, "Bench", 3, 10, 60, "")
        viewModel.confirmDeleteWorkout(exercise)
        assertEquals(exercise, viewModel.workoutToDelete.value)
        
        viewModel.confirmDeleteWorkout(null)
        assertNull(viewModel.workoutToDelete.value)
    }

    @Test
    fun `setShowRestDayConfirmation updates showRestDayConfirmation`() = runTest {
        viewModel.setShowRestDayConfirmation(true)
        assertEquals(true, viewModel.showRestDayConfirmation.value)
        
        viewModel.setShowRestDayConfirmation(false)
        assertEquals(false, viewModel.showRestDayConfirmation.value)
    }

    @Test
    fun `addWorkout calls DAO insert with correct data`() = runTest {
        viewModel.selectDay("Mon")
        
        viewModel.addWorkout("Squat", "100", "5", "5")
        
        coVerify {
            workoutDao.insertWorkout(match {
                it.exerciseName == "Squat" &&
                it.dayOfWeek == "Monday" &&
                it.weight == 100 &&
                it.sets == 5 &&
                it.reps == 5 &&
                it.type == "WEIGHT"
            })
        }
        assertEquals(false, viewModel.isAddModalVisible.value)
    }

    @Test
    fun `addWorkout handles malformed numeric input by using 0`() = runTest {
        viewModel.selectDay("Mon")
        
        viewModel.addWorkout("Squat", "abc", "", "reps")
        
        coVerify {
            workoutDao.insertWorkout(match {
                it.exerciseName == "Squat" &&
                it.weight == 0 &&
                it.sets == 0 &&
                it.reps == 0
            })
        }
    }

    @Test
    fun `addWorkout handles negative numeric input by using 0`() = runTest {
        viewModel.selectDay("Mon")
        
        viewModel.addWorkout("Squat", "-50", "-1", "-5")
        
        coVerify {
            workoutDao.insertWorkout(match {
                it.exerciseName == "Squat" &&
                it.weight == 0 &&
                it.sets == 0 &&
                it.reps == 0
            })
        }
    }

    @Test
    fun `addRestDay deletes all workouts for the day and inserts rest day`() = runTest {
        viewModel.selectDay("Tue")
        
        viewModel.addRestDay()
        
        coVerify {
            workoutDao.deleteAllWorkoutsForDay("Tuesday")
            workoutDao.insertWorkout(match {
                it.exerciseName == "Rest Day" &&
                it.dayOfWeek == "Tuesday" &&
                it.type == "REST"
            })
        }
        assertEquals(false, viewModel.showRestDayConfirmation.value)
    }

    @Test
    fun `deleteWorkout calls DAO delete`() = runTest {
        viewModel.selectDay("Wed")
        val exercise = WeightExercise(10, "Deadlift", 1, 5, 140, "")
        
        viewModel.deleteWorkout(exercise)
        
        coVerify {
            workoutDao.deleteWorkout(match {
                it.id == 10 && it.dayOfWeek == "Wednesday"
            })
        }
        assertNull(viewModel.workoutToDelete.value)
    }

    @Test
    fun `updateWorkout calls DAO insert for WeightExercise`() = runTest {
        viewModel.selectDay("Thu")
        val exercise = WeightExercise(5, "Press", 3, 8, 40, "Hard")
        
        viewModel.updateWorkout(exercise)
        
        coVerify {
            workoutDao.insertWorkout(match {
                it.id == 5 &&
                it.dayOfWeek == "Thursday" &&
                it.exerciseName == "Press" &&
                it.weight == 40 &&
                it.sets == 3 &&
                it.reps == 8 &&
                it.notes == "Hard" &&
                it.type == "WEIGHT"
            })
        }
    }

    @Test
    fun `updateWorkout calls DAO insert for RestExercise`() = runTest {
        viewModel.selectDay("Fri")
        val exercise = RestExercise(20, "Rest Day")
        
        viewModel.updateWorkout(exercise)
        
        coVerify {
            workoutDao.insertWorkout(match {
                it.id == 20 &&
                it.dayOfWeek == "Friday" &&
                it.exerciseName == "Rest Day" &&
                it.type == "REST"
            })
        }
    }

    @Test
    fun `onAddRestDayRequested shows confirmation if workouts exist`() = runTest {
        // Need to mock getWorkoutsForDay to return a list
        every { workoutDao.getWorkoutsForDay("Monday") } returns flowOf(listOf(
            WorkoutEntity(id = 1, dayOfWeek = "Monday", exerciseName = "Bench", type = "WEIGHT")
        ))
        
        // Re-create ViewModel to pick up the new flow
        viewModel = WorkoutsViewModel(application, workoutDao)
        viewModel.selectDay("Mon")
        
        // Collect the lazy flow to start it
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.workoutsForSelectedDay.collect {}
        }
        
        // Wait for flow collection
        advanceUntilIdle()
        
        viewModel.onAddRestDayRequested()
        
        assertEquals(true, viewModel.showRestDayConfirmation.value)
        coVerify(exactly = 0) { workoutDao.deleteAllWorkoutsForDay(any()) }
    }

    @Test
    fun `onAddRestDayRequested adds rest day immediately if no workouts exist`() = runTest {
        viewModel.selectDay("Mon")
        
        viewModel.onAddRestDayRequested()
        
        assertEquals(false, viewModel.showRestDayConfirmation.value)
        coVerify { workoutDao.deleteAllWorkoutsForDay("Monday") }
        coVerify { workoutDao.insertWorkout(match { it.type == "REST" }) }
    }
}
