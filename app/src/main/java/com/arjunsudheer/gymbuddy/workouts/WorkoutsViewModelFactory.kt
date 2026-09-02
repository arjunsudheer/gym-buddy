package com.arjunsudheer.gymbuddy.workouts

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class WorkoutsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutsViewModel::class.java)) {
            val db = WorkoutsDB.getDatabase(application)
            @Suppress("UNCHECKED_CAST")
            return WorkoutsViewModel(application, db.workoutDao()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
