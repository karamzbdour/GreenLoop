package com.example.greenloop.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val LIVING_SITUATION = stringPreferencesKey("living_situation")
        private val ALLERGIES = stringPreferencesKey("allergies")
        private val RECIPES_GENERATED_COUNT = intPreferencesKey("recipes_generated_count")
    }

    val livingSituation: Flow<String> = dataStore.data.map { preferences ->
        preferences[LIVING_SITUATION] ?: "Apartment"
    }

    val allergies: Flow<String> = dataStore.data.map { preferences ->
        preferences[ALLERGIES] ?: ""
    }

    val recipesGeneratedCount: Flow<Int> = dataStore.data.map { preferences ->
        preferences[RECIPES_GENERATED_COUNT] ?: 0
    }

    suspend fun updateLivingSituation(situation: String) {
        dataStore.edit { preferences ->
            preferences[LIVING_SITUATION] = situation
        }
    }

    suspend fun updateAllergies(allergies: String) {
        dataStore.edit { preferences ->
            preferences[ALLERGIES] = allergies
        }
    }

    suspend fun incrementRecipesGenerated() {
        dataStore.edit { preferences ->
            val current = preferences[RECIPES_GENERATED_COUNT] ?: 0
            preferences[RECIPES_GENERATED_COUNT] = current + 1
        }
    }
}
