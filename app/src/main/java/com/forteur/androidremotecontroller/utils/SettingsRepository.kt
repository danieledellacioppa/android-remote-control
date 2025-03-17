package com.forteur.androidremotecontroller.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private val SERVER_KEY = stringPreferencesKey("server_address")

    val serverAddress: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[SERVER_KEY] ?: "http://akhterlauncherota.duckdns.org:12348" }

    suspend fun setServerAddress(newAddress: String) {
        context.dataStore.edit { preferences ->
            preferences[SERVER_KEY] = newAddress
        }
    }
}
