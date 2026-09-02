package com.arjunsudheer.gymbuddy.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.arjunsudheer.gymbuddy.workouts.WorkoutsDB

class SettingsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            val repository = SettingsRepository(context)
            val db = WorkoutsDB.getDatabase(context)
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository, db.workoutDao()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
