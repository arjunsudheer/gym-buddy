package com.arjunsudheer.gymbuddy.workouts

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arjunsudheer.gymbuddy.navigation.TopDaySelector
import com.arjunsudheer.gymbuddy.workouts.components.AddExerciseModal
import com.arjunsudheer.gymbuddy.workouts.components.DeleteConfirmationDialog
import com.arjunsudheer.gymbuddy.workouts.components.RestDayConfirmationDialog
import com.arjunsudheer.gymbuddy.workouts.components.WorkoutCard
import com.arjunsudheer.gymbuddy.workouts.models.RestExercise

@Composable
fun WorkoutsScreen(
    modifier: Modifier = Modifier,
    weightUnit: String = "lbs",
    viewModel: WorkoutsViewModel = viewModel(
        factory = WorkoutsViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val workouts by viewModel.workoutsForSelectedDay.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val expandedId by viewModel.expandedWorkoutId.collectAsState()
    val isAddVisible by viewModel.isAddModalVisible.collectAsState()
    val workoutToDelete by viewModel.workoutToDelete.collectAsState()
    val showRestDayConfirmation by viewModel.showRestDayConfirmation.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddModal(true) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Exercise")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TopDaySelector(
                days = viewModel.daysOfWeek,
                selectedDay = selectedDay,
                onDaySelected = { viewModel.selectDay(it) }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(workouts, key = { it.id }) { workout ->
                    val dismissState = rememberSwipeToDismissBoxState()

                    LaunchedEffect(dismissState.currentValue) {
                        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                            viewModel.confirmDeleteWorkout(workout)
                            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                        }
                    }

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                                MaterialTheme.colorScheme.error
                            } else Color.Transparent

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(color)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    ) {
                        WorkoutCard(
                            exercise = workout,
                            isExpanded = expandedId == workout.id,
                            weightUnit = weightUnit,
                            onToggleExpand = { viewModel.toggleWorkoutExpansion(workout.id) },
                            onUpdate = { viewModel.updateWorkout(it) }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) } // Space for FAB
            }
        }
    }

    if (isAddVisible) {
        val isRestDay = workouts.any { it is RestExercise }
        AddExerciseModal(
            isRestDay = isRestDay,
            weightUnit = weightUnit,
            onDismiss = { viewModel.showAddModal(false) },
            onSave = { name, weight, sets, reps ->
                viewModel.addWorkout(name, weight, sets, reps)
            },
            onAddRestDay = {
                viewModel.onAddRestDayRequested()
                viewModel.showAddModal(false)
            }
        )
    }

    if (showRestDayConfirmation) {
        RestDayConfirmationDialog(
            onDismiss = { viewModel.setShowRestDayConfirmation(false) },
            onConfirm = { viewModel.addRestDay() }
        )
    }

    workoutToDelete?.let { workout ->
        DeleteConfirmationDialog(
            onDismiss = { viewModel.confirmDeleteWorkout(null) },
            onConfirm = { viewModel.deleteWorkout(workout) }
        )
    }
}
