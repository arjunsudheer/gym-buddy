package com.arjunsudheer.gymbuddy.workouts.models

sealed interface Exercise {
    val id: Int
    val name: String
}

data class WeightExercise(
    override val id: Int,
    override val name: String,
    val sets: Int,
    val reps: Int,
    val weight: Int,
    val notes: String
) : Exercise

data class RestExercise(
    override val id: Int,
    override val name: String = "Rest Day"
) : Exercise
