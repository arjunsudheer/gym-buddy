package com.example.gym_buddy.workouts

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gym_buddy.ui.theme.GymbuddyTheme

@Composable
fun WorkoutItem(modifier: Modifier = Modifier) {
    // State for the "Enter a workout" TextField
    var workoutName by remember { mutableStateOf("") }

    // States for the numeric TextFields
    var reps by remember { mutableStateOf("") }
    var sets by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    Column(modifier = modifier.padding(16.dp)) {
        TextField(
            value = workoutName,
            onValueChange = { workoutName = it }, // Update state on change
            label = { Text("Enter a workout") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .horizontalScroll(rememberScrollState()), // Make row scrollable if content overflows
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Reps: ", modifier = Modifier.padding(end = 4.dp))
            TextField(
                value = reps,
                onValueChange = { newValue ->
                    // Allow only digits or empty string
                    if (newValue.all { it.isDigit() }) {
                        reps = newValue
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )

            Text("Sets: ", modifier = Modifier.padding(start = 8.dp, end = 4.dp))
            TextField(
                value = sets,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) {
                        sets = newValue
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )

            Text("Weight: ", modifier = Modifier.padding(start = 8.dp, end = 4.dp))
            TextField(
                value = weight,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) {
                        weight = newValue
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GymbuddyTheme {
        WorkoutItem()
    }
}