package com.arjunsudheer.gymbuddy.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
    }

    val weightUnit: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[WEIGHT_UNIT] ?: "lbs"
        }

    suspend fun setWeightUnit(unit: String) {
        context.dataStore.edit { preferences ->
            preferences[WEIGHT_UNIT] = unit
        }
    }
}
