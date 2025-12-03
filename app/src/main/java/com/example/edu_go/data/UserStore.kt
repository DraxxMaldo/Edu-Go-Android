package com.example.edu_go.data



import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensión para crear el DataStore una sola vez
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserStore(private val context: Context) {

    companion object {
        val USER_ID_KEY = stringPreferencesKey("user_id")
        val TOKEN_KEY = stringPreferencesKey("user_token")
    }

    // Obtener Token
    val getAccessToken: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[TOKEN_KEY]
        }

    // Obtener User ID
    val getUserId: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_ID_KEY]
        }

    // Guardar sesión
    suspend fun saveUser(token: String, userId: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId
        }
    }

    // Borrar sesión
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}