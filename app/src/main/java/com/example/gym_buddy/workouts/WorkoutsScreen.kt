package com.example.gym_buddy.workouts

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
    val workouts by viewModel.workoutsForSelectedDay.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val expandedId by viewModel.expandedWorkoutId.collectAsState()
    val isAddVisible by viewModel.isAddModalVisible.collectAsState()
    val workoutToDelete by viewModel.workoutToDelete.collectAsState()

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
            WeeklySelector(
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
                    WorkoutCard(
                        workout = workout,
                        isExpanded = expandedId == workout.id,
                        onToggleExpand = { viewModel.toggleWorkoutExpansion(workout.id) },
                        onDelete = { viewModel.confirmDeleteWorkout(workout) },
                        onUpdate = { viewModel.updateWorkout(it) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) } // Space for FAB
            }
        }
    }

    if (isAddVisible) {
        AddExerciseModal(
            onDismiss = { viewModel.showAddModal(false) },
            onSave = { name, weight, sets, reps ->
                viewModel.addWorkout(name, weight, sets, reps)
            }
        )
    }

    workoutToDelete?.let { workout ->
        DeleteConfirmationDialog(
            onDismiss = { viewModel.confirmDeleteWorkout(null) },
            onConfirm = { viewModel.deleteWorkout(workout) }
        )
    }
}

@Composable
fun WeeklySelector(
    days: List<String>,
    selectedDay: String,
    onDaySelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(days) { day ->
            val isSelected = day == selectedDay
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.surface
                    )
                    .clickable { onDaySelected(day) }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    color = if (isSelected) Color.Black else Color.White,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun WorkoutCard(
    workout: WorkoutEntity,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: (WorkoutEntity) -> Unit
) {
    var exerciseName by remember(workout.id, workout.exerciseName) { mutableStateOf(workout.exerciseName) }
    var weight by remember(workout.id, workout.weight) { mutableStateOf(workout.weight.toString()) }
    var sets by remember(workout.id, workout.sets) { mutableStateOf(workout.sets.toString()) }
    var reps by remember(workout.id, workout.reps) { mutableStateOf(workout.reps.toString()) }
    var notes by remember(workout.id, workout.notes) { mutableStateOf(workout.notes) }

    LaunchedEffect(exerciseName, weight, sets, reps, notes) {
        val w = weight.toIntOrNull() ?: workout.weight
        val s = sets.toIntOrNull() ?: workout.sets
        val r = reps.toIntOrNull() ?: workout.reps
        if (exerciseName != workout.exerciseName || w != workout.weight || 
            s != workout.sets || r != workout.reps || notes != workout.notes) {
            onUpdate(workout.copy(
                exerciseName = exerciseName,
                weight = w,
                sets = s,
                reps = r,
                notes = notes
            ))
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onToggleExpand() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exerciseName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$weight kg • $sets sets • $reps reps",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp), color = Color.DarkGray)
                    
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Weight", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.secondary,
                            unfocusedBorderColor = Color.DarkGray
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = sets,
                            onValueChange = { sets = it },
                            label = { Text("Sets", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                                unfocusedBorderColor = Color.DarkGray
                            )
                        )
                        OutlinedTextField(
                            value = reps,
                            onValueChange = { reps = it },
                            label = { Text("Reps", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                                unfocusedBorderColor = Color.DarkGray
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.secondary,
                            unfocusedBorderColor = Color.DarkGray
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AddExerciseModal(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var sets by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add New Exercise",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sets,
                        onValueChange = { sets = it },
                        label = { Text("Sets") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = reps,
                        onValueChange = { reps = it },
                        label = { Text("Reps") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { onSave(name, weight, sets, reps) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Save Exercise", color = Color.Black)
                }
                
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remove this exercise?") },
        text = { Text("This action cannot be undone.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Remove", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = Color.White,
        textContentColor = Color.LightGray
    )
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
