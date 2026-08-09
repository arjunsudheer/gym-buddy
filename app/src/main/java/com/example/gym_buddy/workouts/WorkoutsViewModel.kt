package com.example.gym_buddy.workouts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutsViewModel(application: Application) : AndroidViewModel(application) {
    private val workoutDao = WorkoutsDB.getDatabase(application).workoutDao()

    val daysOfWeek =
        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    private val fullDaysOfWeek =
        listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    private val dayMapping = daysOfWeek.zip(fullDaysOfWeek).toMap()

    private val _selectedDay = MutableStateFlow(daysOfWeek[0])
    val selectedDay: StateFlow<String> = _selectedDay

    private val _workoutsByDay = MutableStateFlow<Map<String, List<WorkoutEntity>>>(emptyMap())
    
    val workoutsForSelectedDay: StateFlow<List<WorkoutEntity>> = combine(
        _selectedDay,
        _workoutsByDay
    ) { selected, allWorkouts ->
        val fullDay = dayMapping[selected] ?: "Monday"
        allWorkouts[fullDay] ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _expandedWorkoutId = MutableStateFlow<Int?>(null)
    val expandedWorkoutId: StateFlow<Int?> = _expandedWorkoutId

    private val _isAddModalVisible = MutableStateFlow(false)
    val isAddModalVisible: StateFlow<Boolean> = _isAddModalVisible

    private val _workoutToDelete = MutableStateFlow<WorkoutEntity?>(null)
    val workoutToDelete: StateFlow<WorkoutEntity?> = _workoutToDelete

    init {
        // Observe workouts for each day and update the map
        viewModelScope.launch {
            val flows = fullDaysOfWeek.map { day ->
                workoutDao.getWorkoutsForDay(day)
            }
            // Combine the flows to update the map whenever any day's list changes
            combine(flows) { lists ->
                fullDaysOfWeek.zip(lists).toMap()
            }.collect { combinedMap ->
                _workoutsByDay.value = combinedMap
            }
        }
    }

    fun selectDay(day: String) {
        _selectedDay.value = day
    }

    fun toggleWorkoutExpansion(id: Int) {
        _expandedWorkoutId.value = if (_expandedWorkoutId.value == id) null else id
    }

    fun showAddModal(show: Boolean) {
        _isAddModalVisible.value = show
    }

    fun confirmDeleteWorkout(workout: WorkoutEntity?) {
        _workoutToDelete.value = workout
    }

    fun addWorkout(exerciseName: String, weight: String, sets: String, reps: String) {
        viewModelScope.launch {
            val fullDay = dayMapping[_selectedDay.value] ?: "Monday"
            workoutDao.insertWorkout(
                WorkoutEntity(
                    dayOfWeek = fullDay,
                    exerciseName = exerciseName,
                    weight = weight.toIntOrNull() ?: 0,
                    sets = sets.toIntOrNull() ?: 0,
                    reps = reps.toIntOrNull() ?: 0
                )
            )
            showAddModal(false)
        }
    }

    fun deleteWorkout(workout: WorkoutEntity) {
        viewModelScope.launch {
            workoutDao.deleteWorkout(workout)
            confirmDeleteWorkout(null)
        }
    }

    fun updateWorkout(workout: WorkoutEntity) {
        viewModelScope.launch {
            workoutDao.insertWorkout(workout)
        }
    }
}
