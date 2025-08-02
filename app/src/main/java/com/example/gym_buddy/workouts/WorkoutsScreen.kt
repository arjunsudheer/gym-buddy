package com.example.gym_buddy.workouts

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun WorkoutsScreen(
    modifier: Modifier = Modifier,
    viewModel: WorkoutsViewModel = viewModel(
        factory = WorkoutsViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val workoutsByDay by viewModel.workoutsByDay.collectAsState()
    val days = viewModel.daysOfWeek

    LazyColumn(modifier = modifier.padding(16.dp)) {
        days.forEach { day ->
            stickyHeader { // Make day headers sticky
                DayHeader(day = day)
            }
            item {
                Button(
                    onClick = { viewModel.addWorkout(day) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("Add Workout for $day")
                }
            }
            items(
                items = workoutsByDay[day] ?: emptyList(),
                key = { workout -> workout.id }
            ) { workout ->
                WorkoutItem(
                    workout = workout,
                    onDelete = { viewModel.deleteWorkout(workout) },
                    onUpdate = { updatedWorkout -> viewModel.updateWorkout(updatedWorkout) },
                )
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            }
            item {
                Spacer(modifier = Modifier.height(16.dp)) // Add space between days
            }
        }
    }
}

@Composable
fun DayHeader(day: String, modifier: Modifier = Modifier) {
    Text(
        text = day,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)) // Semi-transparent background
            .padding(vertical = 8.dp, horizontal = 4.dp)
    )
}

@Composable
fun WorkoutItem(
    workout: WorkoutEntity,
    onDelete: () -> Unit,
    onUpdate: (WorkoutEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    // Local state for text fields, remembered against workout.id to reset when the item changes
    var exerciseName by remember(
        workout.id,
        workout.exerciseName
    ) { mutableStateOf(workout.exerciseName) }
    var sets by remember(workout.id, workout.sets) { mutableStateOf(workout.sets) }
    var reps by remember(workout.id, workout.reps) { mutableStateOf(workout.reps) }
    var weight by remember(
        workout.id,
        workout.weight
    ) { mutableStateOf(workout.weight) }

    // LaunchedEffect will trigger when any of the text field states change
    LaunchedEffect(exerciseName, sets, reps, weight) {
        // Update if the content has actually changed from the original workout prop
        if (exerciseName != workout.exerciseName || sets != workout.sets || reps != workout.reps ||
            weight != workout.weight
        ) {
            onUpdate(
                workout.copy(
                    exerciseName = exerciseName.trim(),
                    sets = sets.trim(),
                    reps = reps.trim(),
                    weight = weight.trim(),
                )
            )
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Use full row for exercise name field
            OutlinedTextField(
                value = exerciseName,
                onValueChange = { exerciseName = it },
                label = { Text("Exercise Name") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Row for sets and reps
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = sets,
                    onValueChange = { sets = it },
                    label = { Text("Sets") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text("Reps") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Row for weight and delete workout icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.7f),
                )
                IconButton(onClick = onDelete, modifier = Modifier.weight(0.3f)) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete Workout",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

class WorkoutsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkoutsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}