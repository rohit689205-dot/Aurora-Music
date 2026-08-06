package com.example.ui.settings

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = application.dataStore
    
    val dynamicColor: StateFlow<Boolean> = getPreference(booleanPreferencesKey("dynamic_color"), false)
    val gaplessPlayback: StateFlow<Boolean> = getPreference(booleanPreferencesKey("gapless_playback"), true)
    val pauseOnUnplug: StateFlow<Boolean> = getPreference(booleanPreferencesKey("pause_on_unplug"), true)
    val wifiOnlyDownloads: StateFlow<Boolean> = getPreference(booleanPreferencesKey("wifi_only_downloads"), true)
    val offlineMode: StateFlow<Boolean> = getPreference(booleanPreferencesKey("offline_mode"), false)
    
    val playbackNotifications: StateFlow<Boolean> = getPreference(booleanPreferencesKey("playback_notifications"), true)
    val newRecommendations: StateFlow<Boolean> = getPreference(booleanPreferencesKey("new_recommendations"), false)
    val largeText: StateFlow<Boolean> = getPreference(booleanPreferencesKey("large_text"), false)

    private fun <T> getPreference(key: Preferences.Key<T>, defaultValue: T): StateFlow<T> {
        return dataStore.data.map { preferences ->
            preferences[key] ?: defaultValue
        }.stateIn(viewModelScope, SharingStarted.Eagerly, defaultValue)
    }

    fun setDynamicColor(value: Boolean) = setPreference(booleanPreferencesKey("dynamic_color"), value)
    fun setGaplessPlayback(value: Boolean) = setPreference(booleanPreferencesKey("gapless_playback"), value)
    fun setPauseOnUnplug(value: Boolean) = setPreference(booleanPreferencesKey("pause_on_unplug"), value)
    fun setWifiOnlyDownloads(value: Boolean) = setPreference(booleanPreferencesKey("wifi_only_downloads"), value)
    fun setOfflineMode(value: Boolean) = setPreference(booleanPreferencesKey("offline_mode"), value)
    
    fun setPlaybackNotifications(value: Boolean) = setPreference(booleanPreferencesKey("playback_notifications"), value)
    fun setNewRecommendations(value: Boolean) = setPreference(booleanPreferencesKey("new_recommendations"), value)
    fun setLargeText(value: Boolean) = setPreference(booleanPreferencesKey("large_text"), value)
    
    private fun <T> setPreference(key: Preferences.Key<T>, value: T) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[key] = value
            }
        }
    }
}
