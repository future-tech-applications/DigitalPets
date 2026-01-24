package com.learning.companionshimejis.persistence

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object PreferencesManager {
    private val KEY_IS_ENABLED = booleanPreferencesKey("is_service_enabled")
    private val KEY_SELECTED_PETS = stringSetPreferencesKey("selected_pets")
    private val KEY_PET_SCALE = floatPreferencesKey("pet_scale")
    private val KEY_PET_OPACITY =
        floatPreferencesKey("pet_opacity")
    private val KEY_ANIMATION_SPEED =
        floatPreferencesKey("animation_speed")

    fun isServiceEnabled(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences -> preferences[KEY_IS_ENABLED] ?: false }
    }

    suspend fun setServiceEnabled(context: Context, enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[KEY_IS_ENABLED] = enabled }
    }

    fun getSelectedPets(context: Context): Flow<Set<String>> {
        return context.dataStore.data.map { preferences ->
            preferences[KEY_SELECTED_PETS] ?: emptySet()
        }
    }

    suspend fun setSelectedPets(context: Context, petIds: Set<String>) {
        context.dataStore.edit { preferences -> preferences[KEY_SELECTED_PETS] = petIds }
    }

    fun getPetScale(context: Context): Flow<Float> {
        return context.dataStore.data.map { preferences -> preferences[KEY_PET_SCALE] ?: 1.0f }
    }

    suspend fun setPetScale(context: Context, scale: Float) {
        context.dataStore.edit { preferences -> preferences[KEY_PET_SCALE] = scale }
    }

    fun getPetOpacity(context: Context): Flow<Float> {
        return context.dataStore.data.map { preferences -> preferences[KEY_PET_OPACITY] ?: 1.0f }
    }

    suspend fun setPetOpacity(context: Context, opacity: Float) {
        context.dataStore.edit { preferences -> preferences[KEY_PET_OPACITY] = opacity }
    }

    fun getAnimationSpeed(context: Context): Flow<Float> {
        return context.dataStore.data.map { preferences ->
            preferences[KEY_ANIMATION_SPEED] ?: 1.0f
        }
    }

    suspend fun setAnimationSpeed(context: Context, speed: Float) {
        context.dataStore.edit { preferences -> preferences[KEY_ANIMATION_SPEED] = speed }
    }

    fun getPetPosition(context: Context, petId: String): Flow<Pair<Int, Int>?> {
        val keyX = intPreferencesKey("pet_pos_x_$petId")
        val keyY = intPreferencesKey("pet_pos_y_$petId")
        return context.dataStore.data.map { preferences ->
            val x = preferences[keyX]
            val y = preferences[keyY]
            if (x != null && y != null) x to y else null
        }
    }

    suspend fun setPetPosition(context: Context, petId: String, x: Int, y: Int) {
        val keyX = intPreferencesKey("pet_pos_x_$petId")
        val keyY = intPreferencesKey("pet_pos_y_$petId")
        context.dataStore.edit { preferences ->
            preferences[keyX] = x
            preferences[keyY] = y
        }
    }
}
