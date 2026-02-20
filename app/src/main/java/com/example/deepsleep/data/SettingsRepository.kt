package com.example.deepsleep.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.deepsleep.model.AppSettings
import com.example.deepsleep.model.WhitelistType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    
    private object PreferencesKeys {
        val DEBOUNCE_INTERVAL = intPreferencesKey("debounce_interval")
        val SUPPRESS_ENABLED = booleanPreferencesKey("suppress_enabled")
        val SUPPRESS_MODE = stringPreferencesKey("suppress_mode")
        val SUPPRESS_INTERVAL = intPreferencesKey("suppress_interval")
        val SUPPRESS_OOM_VALUE = intPreferencesKey("suppress_oom_value")
        val BG_OPT_ENABLED = booleanPreferencesKey("bg_opt_enabled")
        val MOTION_BACKUP = stringPreferencesKey("motion_backup")
    }
    
    val settings: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            debounceInterval = preferences[PreferencesKeys.DEBOUNCE_INTERVAL] ?: 3,
            suppressEnabled = preferences[PreferencesKeys.SUPPRESS_ENABLED] ?: true,
            suppressMode = preferences[PreferencesKeys.SUPPRESS_MODE] ?: "conservative",
            suppressInterval = preferences[PreferencesKeys.SUPPRESS_INTERVAL] ?: 60,
            suppressOomValue = preferences[PreferencesKeys.SUPPRESS_OOM_VALUE] ?: 800,
            backgroundOptimizationEnabled = preferences[PreferencesKeys.BG_OPT_ENABLED] ?: true,
            motionBackup = preferences[PreferencesKeys.MOTION_BACKUP] ?: "enabled"
        )
    }
    
    suspend fun getSettings(): AppSettings = settings.first()
    
    suspend fun setDebounceInterval(interval: Int) {
        context.dataStore.edit { it[PreferencesKeys.DEBOUNCE_INTERVAL] = interval }
    }
    
    suspend fun setSuppressEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SUPPRESS_ENABLED] = enabled }
    }
    
    suspend fun setSuppressMode(mode: String) {
        context.dataStore.edit { it[PreferencesKeys.SUPPRESS_MODE] = mode }
    }
    
    suspend fun setSuppressInterval(interval: Int) {
        context.dataStore.edit { it[PreferencesKeys.SUPPRESS_INTERVAL] = interval }
    }
    
    suspend fun setSuppressOomValue(value: Int) {
        context.dataStore.edit { it[PreferencesKeys.SUPPRESS_OOM_VALUE] = value }
    }
    
    suspend fun setBackgroundOptimizationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.BG_OPT_ENABLED] = enabled }
    }
    
    suspend fun saveMotionBackup(state: String) {
        context.dataStore.edit { it[PreferencesKeys.MOTION_BACKUP] = state }
    }
    
    suspend fun getMotionBackup(): String {
        return context.dataStore.data.first()[PreferencesKeys.MOTION_BACKUP] ?: "enabled"
    }
    
    suspend fun getSuppressWhitelist(): List<String> {
        return WhitelistRepository().loadItems(context, WhitelistType.SUPPRESS).map { it.name }
    }
    
    suspend fun getBackgroundWhitelist(): List<String> {
        return WhitelistRepository().loadItems(context, WhitelistType.BACKGROUND).map { it.name }
    }
}
