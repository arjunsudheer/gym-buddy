package com.example.gym_buddy.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gym_buddy.workouts.WorkoutDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val workoutDao: WorkoutDao
) : ViewModel() {

    val weightUnit: StateFlow<String> = repository.weightUnit
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "lbs"
        )

    fun setWeightUnit(unit: String) {
        viewModelScope.launch {
            val currentUnit = weightUnit.value
            if (currentUnit != unit) {
                // Perform conversion
                val workouts = workoutDao.getAllWorkouts()
                val updatedWorkouts = workouts.map { workout ->
                    if (workout.type == "WEIGHT") {
                        val newWeight = if (unit == "kg") {
                            (workout.weight * 0.453592).roundToInt()
                        } else {
                            (workout.weight * 2.20462).roundToInt()
                        }
                        workout.copy(weight = newWeight)
                    } else {
                        workout
                    }
                }
                workoutDao.updateWorkouts(updatedWorkouts)
                repository.setWeightUnit(unit)
            }
        }
    }
}
