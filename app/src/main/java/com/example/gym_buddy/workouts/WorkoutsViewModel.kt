package com.example.gym_buddy.workouts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class WorkoutsViewModel(application: Application) : AndroidViewModel(application) {
    private val workoutDao = WorkoutsDB.getDatabase(application).workoutDao()

    val daysOfWeek =
        listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    // A map to hold the StateFlow for each day's workouts
    private val _workoutsByDay = MutableStateFlow<Map<String, List<WorkoutEntity>>>(emptyMap())
    val workoutsByDay: StateFlow<Map<String, List<WorkoutEntity>>> = _workoutsByDay

    init {
        // Observe workouts for each day and update the map
        viewModelScope.launch {
            val flows = daysOfWeek.map { day ->
                workoutDao.getWorkoutsForDay(day)
            }
            // Combine the flows to update the map whenever any day's list changes
            combine(flows) { lists ->
                daysOfWeek.zip(lists).toMap()
            }.collect { combinedMap ->
                _workoutsByDay.value = combinedMap
            }
        }
    }

    fun addWorkout(day: String) {
        viewModelScope.launch {
            workoutDao.insertWorkout(WorkoutEntity(dayOfWeek = day))
        }
    }

    fun deleteWorkout(workout: WorkoutEntity) {
        viewModelScope.launch {
            workoutDao.deleteWorkout(workout)
        }
    }

    fun updateWorkout(workout: WorkoutEntity) {
        viewModelScope.launch {
            // Assumes WorkoutEntity has a primary key that Room can use for updates
            // If the workout already exists, insert with OnConflictStrategy.REPLACE will update it
            workoutDao.insertWorkout(workout)
        }
    }
}