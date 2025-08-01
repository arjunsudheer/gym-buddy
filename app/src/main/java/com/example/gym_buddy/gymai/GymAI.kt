package com.example.gym_buddy.gymai

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch

@Composable
fun GymAI(generativeModel: GenerativeModel) {
    // Use rememberSaveable to retain most recent chat on a phone rotation
    var query by rememberSaveable { mutableStateOf("") }
    var response: String? by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Display area for the AI's response
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                response?.let {
                    Text(text = it, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Text input and send button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Ask GymAI") },
                modifier = Modifier.weight(1f),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (query.isNotBlank()) {
                                coroutineScope.launch {
                                    isLoading = true
                                    try {
                                        // Pass context to your function
                                        response = askGymAI(generativeModel, query)
                                    } catch (e: Exception) {
                                        response = "Error: ${e.message}"
                                        // Handle exceptions appropriately
                                        e.printStackTrace()
                                    }
                                    isLoading = false
                                    // Set the query to an empty string after processing
                                    query = ""
                                }
                            }
                        },
                        enabled = query.isNotBlank() && !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send"
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Disclaimer text
        Text(
            text = "GymAI may make mistakes. Always double check for correct information.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

suspend fun askGymAI(model: GenerativeModel, query: String): String? {
    val prompt =
        "You are an expert in gym related information." +
                "Provide your answer in a brief 2 sentence explanation. You can also use up to 3 bullet points." +
                "Be specific, informative, and relevant. Use plain text, don't use markdown.\n " +
                "User query: $query"

    try {
        // Use the passed-in model instance
        val apiResponse = model.generateContent(prompt)
        return apiResponse.text
    } catch (_: Exception) {
        return "There was an error with GymAI. Please try again after some time."
    }
}