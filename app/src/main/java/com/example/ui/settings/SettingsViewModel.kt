package com.example.ui.settings

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MusicDatabase
import com.example.data.MusicRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = application.dataStore
    private val repository = MusicRepository(MusicDatabase.getDatabase(application).songDao())

    val themeMode: StateFlow<String> = getPreference(stringPreferencesKey("theme_mode"), "System Default")
    val uiDensity: StateFlow<String> = getPreference(stringPreferencesKey("ui_density"), "Comfortable")
    val accentColor: StateFlow<String> = getPreference(stringPreferencesKey("accent_color"), "Emerald Green")
    
    val dynamicColor: StateFlow<Boolean> = getPreference(booleanPreferencesKey("dynamic_color"), false)
    val gaplessPlayback: StateFlow<Boolean> = getPreference(booleanPreferencesKey("gapless_playback"), true)
    val crossfade: StateFlow<String> = getPreference(stringPreferencesKey("crossfade"), "Off")
    val pauseOnUnplug: StateFlow<Boolean> = getPreference(booleanPreferencesKey("pause_on_unplug"), true)
    
    val audioQuality: StateFlow<String> = getPreference(stringPreferencesKey("audio_quality"), "High")
    val equalizerEnabled: StateFlow<Boolean> = getPreference(booleanPreferencesKey("equalizer_enabled"), true)
    val equalizerPreset: StateFlow<String> = getPreference(stringPreferencesKey("equalizer_preset"), "Flat")
    val eqBand60Hz: StateFlow<Float> = getPreference(floatPreferencesKey("eq_band_60hz"), 0f)
    val eqBand230Hz: StateFlow<Float> = getPreference(floatPreferencesKey("eq_band_230hz"), 0f)
    val eqBand910Hz: StateFlow<Float> = getPreference(floatPreferencesKey("eq_band_910hz"), 0f)
    val eqBand4kHz: StateFlow<Float> = getPreference(floatPreferencesKey("eq_band_4khz"), 0f)
    val eqBand14kHz: StateFlow<Float> = getPreference(floatPreferencesKey("eq_band_14khz"), 0f)
    val eqBassBoost: StateFlow<Float> = getPreference(floatPreferencesKey("eq_bass_boost"), 0f)
    val eq3dSurround: StateFlow<Float> = getPreference(floatPreferencesKey("eq_3d_surround"), 0f)

    val wifiOnlyDownloads: StateFlow<Boolean> = getPreference(booleanPreferencesKey("wifi_only_downloads"), true)
    val downloadQuality: StateFlow<String> = getPreference(stringPreferencesKey("download_quality"), "High")
    val offlineMode: StateFlow<Boolean> = getPreference(booleanPreferencesKey("offline_mode"), false)

    val playbackNotifications: StateFlow<Boolean> = getPreference(booleanPreferencesKey("playback_notifications"), true)
    val newRecommendations: StateFlow<Boolean> = getPreference(booleanPreferencesKey("new_recommendations"), false)
    val largeText: StateFlow<Boolean> = getPreference(booleanPreferencesKey("large_text"), false)

    val debugLogging: StateFlow<Boolean> = getPreference(booleanPreferencesKey("debug_logging"), true)
    val mockDataMode: StateFlow<Boolean> = getPreference(booleanPreferencesKey("mock_data_mode"), false)

    private fun <T> getPreference(key: Preferences.Key<T>, defaultValue: T): StateFlow<T> {
        return dataStore.data.map { preferences ->
            preferences[key] ?: defaultValue
        }.stateIn(viewModelScope, SharingStarted.Eagerly, defaultValue)
    }

    fun setThemeMode(value: String) = setPreference(stringPreferencesKey("theme_mode"), value)
    fun setUiDensity(value: String) = setPreference(stringPreferencesKey("ui_density"), value)
    fun setAccentColor(value: String) = setPreference(stringPreferencesKey("accent_color"), value)

    fun setDynamicColor(value: Boolean) = setPreference(booleanPreferencesKey("dynamic_color"), value)
    fun setGaplessPlayback(value: Boolean) = setPreference(booleanPreferencesKey("gapless_playback"), value)
    fun setCrossfade(value: String) = setPreference(stringPreferencesKey("crossfade"), value)
    fun setPauseOnUnplug(value: Boolean) = setPreference(booleanPreferencesKey("pause_on_unplug"), value)

    fun setAudioQuality(value: String) = setPreference(stringPreferencesKey("audio_quality"), value)
    fun setEqualizerEnabled(value: Boolean) = setPreference(booleanPreferencesKey("equalizer_enabled"), value)

    fun applyEqualizerPreset(preset: String) {
        setPreference(stringPreferencesKey("equalizer_preset"), preset)
        val (b60, b230, b910, b4k, b14k) = when (preset) {
            "Bass Boost" -> listOf(+6f, +4f, 0f, -1f, -2f)
            "Vocal" -> listOf(-2f, +2f, +5f, +3f, -1f)
            "Treble" -> listOf(-2f, -1f, +1f, +5f, +8f)
            "Rock" -> listOf(+5f, +3f, -1f, +3f, +5f)
            "Jazz" -> listOf(+3f, +2f, +1f, +2f, +3f)
            "Electronic" -> listOf(+4f, +2f, 0f, +2f, +4f)
            else -> listOf(0f, 0f, 0f, 0f, 0f) // Flat / Default
        }
        setPreference(floatPreferencesKey("eq_band_60hz"), b60)
        setPreference(floatPreferencesKey("eq_band_230hz"), b230)
        setPreference(floatPreferencesKey("eq_band_910hz"), b910)
        setPreference(floatPreferencesKey("eq_band_4khz"), b4k)
        setPreference(floatPreferencesKey("eq_band_14khz"), b14k)
    }

    fun setEqBand60Hz(value: Float) {
        setPreference(floatPreferencesKey("eq_band_60hz"), value)
        setPreference(stringPreferencesKey("equalizer_preset"), "Custom")
    }
    fun setEqBand230Hz(value: Float) {
        setPreference(floatPreferencesKey("eq_band_230hz"), value)
        setPreference(stringPreferencesKey("equalizer_preset"), "Custom")
    }
    fun setEqBand910Hz(value: Float) {
        setPreference(floatPreferencesKey("eq_band_910hz"), value)
        setPreference(stringPreferencesKey("equalizer_preset"), "Custom")
    }
    fun setEqBand4kHz(value: Float) {
        setPreference(floatPreferencesKey("eq_band_4khz"), value)
        setPreference(stringPreferencesKey("equalizer_preset"), "Custom")
    }
    fun setEqBand14kHz(value: Float) {
        setPreference(floatPreferencesKey("eq_band_14khz"), value)
        setPreference(stringPreferencesKey("equalizer_preset"), "Custom")
    }

    fun setEqBassBoost(value: Float) = setPreference(floatPreferencesKey("eq_bass_boost"), value)
    fun setEq3dSurround(value: Float) = setPreference(floatPreferencesKey("eq_3d_surround"), value)

    fun setWifiOnlyDownloads(value: Boolean) = setPreference(booleanPreferencesKey("wifi_only_downloads"), value)
    fun setDownloadQuality(value: String) = setPreference(stringPreferencesKey("download_quality"), value)
    fun setOfflineMode(value: Boolean) = setPreference(booleanPreferencesKey("offline_mode"), value)

    fun setPlaybackNotifications(value: Boolean) = setPreference(booleanPreferencesKey("playback_notifications"), value)
    fun setNewRecommendations(value: Boolean) = setPreference(booleanPreferencesKey("new_recommendations"), value)
    fun setLargeText(value: Boolean) = setPreference(booleanPreferencesKey("large_text"), value)

    fun setDebugLogging(value: Boolean) = setPreference(booleanPreferencesKey("debug_logging"), value)
    fun setMockDataMode(value: Boolean) = setPreference(booleanPreferencesKey("mock_data_mode"), value)

    fun clearListeningHistory(onCleared: () -> Unit = {}) {
        viewModelScope.launch {
            repository.clearHistory()
            repository.clearSearchHistory()
            onCleared()
        }
    }

    fun clearDownloads(onCleared: () -> Unit = {}) {
        viewModelScope.launch {
            repository.clearAllDownloads()
            onCleared()
        }
    }

    private fun <T> setPreference(key: Preferences.Key<T>, value: T) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[key] = value
            }
        }
    }
}

