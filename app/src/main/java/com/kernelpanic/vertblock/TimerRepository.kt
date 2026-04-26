package com.kernelpanic.vertblock

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Расширение для создания DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "timer")

class TimerRepository(private val context: Context) {

    // Переименовали ключ, чтобы убрать ошибку стиля
    private val remainingSecondsKey = intPreferencesKey("remaining_seconds")

    // Поток, который автоматически обновляется при изменении данных
    val remainingSeconds: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[remainingSecondsKey] ?: 0
    }

    // Функция для сохранения оставшегося времени
    suspend fun saveRemaining(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[remainingSecondsKey] = seconds
        }
    }
}