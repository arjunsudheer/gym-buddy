package com.example.gym_buddy.workouts

import android.app.Application
import java.util.Calendar
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gym_buddy.workouts.models.Exercise
import com.example.gym_buddy.workouts.models.RestExercise
import com.example.gym_buddy.workouts.models.WeightExercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutsViewModel(
    application: Application,
    private val workoutDao: WorkoutDao
) : AndroidViewModel(application) {

    val daysOfWeek =
        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    private val fullDaysOfWeek =
        listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    private val dayMapping = daysOfWeek.zip(fullDaysOfWeek).toMap()

    private val _selectedDay = MutableStateFlow(
        daysOfWeek[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1]
    )
    val selectedDay: StateFlow<String> = _selectedDay

    private val _workoutsByDay = MutableStateFlow<Map<String, List<WorkoutEntity>>>(emptyMap())
    
    val workoutsForSelectedDay: StateFlow<List<Exercise>> = combine(
        _selectedDay,
        _workoutsByDay
    ) { selected, allWorkouts ->
        val fullDay = dayMapping[selected] ?: "Monday"
        allWorkouts[fullDay]?.map { it.toDomain() } ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _expandedWorkoutId = MutableStateFlow<Int?>(null)
    val expandedWorkoutId: StateFlow<Int?> = _expandedWorkoutId

    private val _isAddModalVisible = MutableStateFlow(false)
    val isAddModalVisible: StateFlow<Boolean> = _isAddModalVisible

    private val _workoutToDelete = MutableStateFlow<Exercise?>(null)
    val workoutToDelete: StateFlow<Exercise?> = _workoutToDelete

    private val _showRestDayConfirmation = MutableStateFlow(false)
    val showRestDayConfirmation: StateFlow<Boolean> = _showRestDayConfirmation

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

    fun confirmDeleteWorkout(workout: Exercise?) {
        _workoutToDelete.value = workout
    }

    fun setShowRestDayConfirmation(show: Boolean) {
        _showRestDayConfirmation.value = show
    }

    fun addWorkout(exerciseName: String, weight: String, sets: String, reps: String) {
        viewModelScope.launch {
            val fullDay = dayMapping[_selectedDay.value] ?: "Monday"
            workoutDao.insertWorkout(
                WorkoutEntity(
                    dayOfWeek = fullDay,
                    exerciseName = exerciseName,
                    weight = (weight.toIntOrNull() ?: 0).coerceAtLeast(0),
                    sets = (sets.toIntOrNull() ?: 0).coerceAtLeast(0),
                    reps = (reps.toIntOrNull() ?: 0).coerceAtLeast(0),
                    type = "WEIGHT"
                )
            )
            showAddModal(false)
        }
    }

    fun onAddRestDayRequested() {
        if (workoutsForSelectedDay.value.isNotEmpty()) {
            _showRestDayConfirmation.value = true
        } else {
            addRestDay()
        }
    }

    fun addRestDay() {
        viewModelScope.launch {
            val fullDay = dayMapping[_selectedDay.value] ?: "Monday"
            workoutDao.deleteAllWorkoutsForDay(fullDay)
            workoutDao.insertWorkout(
                WorkoutEntity(
                    dayOfWeek = fullDay,
                    exerciseName = "Rest Day",
                    type = "REST"
                )
            )
            _showRestDayConfirmation.value = false
        }
    }

    fun deleteWorkout(exercise: Exercise) {
        viewModelScope.launch {
            val fullDay = dayMapping[_selectedDay.value] ?: "Monday"
            // We need to find the entity to delete it, or just use a dummy one with the same ID
            workoutDao.deleteWorkout(
                WorkoutEntity(
                    id = exercise.id,
                    dayOfWeek = fullDay // Required by Entity structure but maybe not by DAO if it only uses ID
                )
            )
            confirmDeleteWorkout(null)
        }
    }

    fun updateWorkout(exercise: Exercise) {
        viewModelScope.launch {
            val fullDay = dayMapping[_selectedDay.value] ?: "Monday"
            val entity = when (exercise) {
                is WeightExercise -> WorkoutEntity(
                    id = exercise.id,
                    dayOfWeek = fullDay,
                    exerciseName = exercise.name,
                    sets = exercise.sets,
                    reps = exercise.reps,
                    weight = exercise.weight,
                    notes = exercise.notes,
                    type = "WEIGHT"
                )
                is RestExercise -> WorkoutEntity(
                    id = exercise.id,
                    dayOfWeek = fullDay,
                    exerciseName = exercise.name,
                    type = "REST"
                )
            }
            workoutDao.insertWorkout(entity)
        }
    }

    private fun WorkoutEntity.toDomain(): Exercise {
        return when (type) {
            "REST" -> RestExercise(id = id)
            else -> WeightExercise(
                id = id,
                name = exerciseName,
                sets = sets,
                reps = reps,
                weight = weight,
                notes = notes
            )
        }
    }
}
