package com.arjunsudheer.gymbuddy.workouts.models

import org.junit.Assert.assertEquals
import org.junit.Test

class ExerciseTest {

    @Test
    fun `WeightExercise properties are correctly assigned`() {
        val exercise = WeightExercise(
            id = 1,
            name = "Bench Press",
            sets = 3,
            reps = 10,
            weight = 60,
            notes = "Feeling strong"
        )

        assertEquals(1, exercise.id)
        assertEquals("Bench Press", exercise.name)
        assertEquals(3, exercise.sets)
        assertEquals(10, exercise.reps)
        assertEquals(60, exercise.weight)
        assertEquals("Feeling strong", exercise.notes)
    }

    @Test
    fun `RestExercise properties are correctly assigned`() {
        val exercise = RestExercise(id = 2)

        assertEquals(2, exercise.id)
        assertEquals("Rest Day", exercise.name)
    }

    @Test
    fun `RestExercise with custom name`() {
        val exercise = RestExercise(id = 3, name = "Active Recovery")

        assertEquals(3, exercise.id)
        assertEquals("Active Recovery", exercise.name)
    }
}
