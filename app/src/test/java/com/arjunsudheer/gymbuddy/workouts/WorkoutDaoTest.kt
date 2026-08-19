package com.arjunsudheer.gymbuddy.workouts

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WorkoutDaoTest {

    private lateinit var workoutDao: WorkoutDao
    private lateinit var db: WorkoutsDB

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, WorkoutsDB::class.java)
            .allowMainThreadQueries()
            .build()
        workoutDao = db.workoutDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `insert and get workouts for day`() = runBlocking {
        val workout = WorkoutEntity(
            dayOfWeek = "Monday",
            exerciseName = "Bench Press",
            sets = 3,
            reps = 10,
            weight = 60,
            type = "WEIGHT"
        )
        workoutDao.insertWorkout(workout)
        
        val workouts = workoutDao.getWorkoutsForDay("Monday").first()
        assertEquals(1, workouts.size)
        assertEquals("Bench Press", workouts[0].exerciseName)
    }

    @Test
    fun `delete workout`() = runBlocking {
        val workout = WorkoutEntity(
            id = 1,
            dayOfWeek = "Monday",
            exerciseName = "Bench Press",
            type = "WEIGHT"
        )
        workoutDao.insertWorkout(workout)
        workoutDao.deleteWorkout(workout)
        
        val workouts = workoutDao.getWorkoutsForDay("Monday").first()
        assertTrue(workouts.isEmpty())
    }

    @Test
    fun `deleteAllWorkoutsForDay clears all workouts for that day`() = runBlocking {
        workoutDao.insertWorkout(WorkoutEntity(dayOfWeek = "Monday", exerciseName = "Ex 1"))
        workoutDao.insertWorkout(WorkoutEntity(dayOfWeek = "Monday", exerciseName = "Ex 2"))
        workoutDao.insertWorkout(WorkoutEntity(dayOfWeek = "Tuesday", exerciseName = "Ex 3"))
        
        workoutDao.deleteAllWorkoutsForDay("Monday")
        
        val mondayWorkouts = workoutDao.getWorkoutsForDay("Monday").first()
        val tuesdayWorkouts = workoutDao.getWorkoutsForDay("Tuesday").first()
        
        assertTrue(mondayWorkouts.isEmpty())
        assertEquals(1, tuesdayWorkouts.size)
    }
}
