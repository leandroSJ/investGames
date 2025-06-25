package com.example.investgames.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {

    companion object {
        private val KEY_USERNAME = stringPreferencesKey("username")
    }

    suspend fun saveUserSession(username: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USERNAME] = username
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_USERNAME)
        }
    }

    suspend fun getUserSession(): String? {
        return context.dataStore.data.map { prefs ->
            prefs[KEY_USERNAME]
        }.first()
    }
}
