package com.example.ui.settings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.Spacing

@Composable
fun SettingsScreen(
    onNavigateToStorage: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(),
    updateViewModel: com.example.update.UpdateViewModel = viewModel()
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val uiDensity by viewModel.uiDensity.collectAsStateWithLifecycle()
    val accentColor by viewModel.accentColor.collectAsStateWithLifecycle()
    val dynamicColor by viewModel.dynamicColor.collectAsStateWithLifecycle()
    val gaplessPlayback by viewModel.gaplessPlayback.collectAsStateWithLifecycle()
    val crossfade by viewModel.crossfade.collectAsStateWithLifecycle()
    val pauseOnUnplug by viewModel.pauseOnUnplug.collectAsStateWithLifecycle()
    
    val audioQuality by viewModel.audioQuality.collectAsStateWithLifecycle()
    val equalizerEnabled by viewModel.equalizerEnabled.collectAsStateWithLifecycle()
    val equalizerPreset by viewModel.equalizerPreset.collectAsStateWithLifecycle()
    val eqBand60Hz by viewModel.eqBand60Hz.collectAsStateWithLifecycle()
    val eqBand230Hz by viewModel.eqBand230Hz.collectAsStateWithLifecycle()
    val eqBand910Hz by viewModel.eqBand910Hz.collectAsStateWithLifecycle()
    val eqBand4kHz by viewModel.eqBand4kHz.collectAsStateWithLifecycle()
    val eqBand14kHz by viewModel.eqBand14kHz.collectAsStateWithLifecycle()
    val eqBassBoost by viewModel.eqBassBoost.collectAsStateWithLifecycle()
    val eq3dSurround by viewModel.eq3dSurround.collectAsStateWithLifecycle()

    val wifiOnlyDownloads by viewModel.wifiOnlyDownloads.collectAsStateWithLifecycle()
    val downloadQuality by viewModel.downloadQuality.collectAsStateWithLifecycle()
    val offlineMode by viewModel.offlineMode.collectAsStateWithLifecycle()

    val playbackNotifications by viewModel.playbackNotifications.collectAsStateWithLifecycle()
    val newRecommendations by viewModel.newRecommendations.collectAsStateWithLifecycle()
    val largeText by viewModel.largeText.collectAsStateWithLifecycle()

    val debugLogging by viewModel.debugLogging.collectAsStateWithLifecycle()
    val mockDataMode by viewModel.mockDataMode.collectAsStateWithLifecycle()

    val providerAudius by viewModel.providerAudius.collectAsStateWithLifecycle()
    val providerJamendo by viewModel.providerJamendo.collectAsStateWithLifecycle()
    val providerYtMusic by viewModel.providerYtMusic.collectAsStateWithLifecycle()
    val providerLastFm by viewModel.providerLastFm.collectAsStateWithLifecycle()
    val providerLrcLib by viewModel.providerLrcLib.collectAsStateWithLifecycle()

    var developerModeUnlockClicks by remember { mutableIntStateOf(0) }
    val isDeveloperMode = developerModeUnlockClicks >= 7
    val context = LocalContext.current

    // Dialog state controllers
    var showThemeDialog by remember { mutableStateOf(false) }
    var showUiDensityDialog by remember { mutableStateOf(false) }
    var showCrossfadeDialog by remember { mutableStateOf(false) }
    var showAudioQualityDialog by remember { mutableStateOf(false) }
    var showEqualizerDialog by remember { mutableStateOf(false) }
    var showDownloadQualityDialog by remember { mutableStateOf(false) }
    var showDiagnosticsDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }

    fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = Spacing.XL, vertical = Spacing.L)
            )
        }

        item {
            SettingsCategory("Appearance")
            SettingsItem(
                icon = Icons.Rounded.Palette,
                title = "Theme",
                subtitle = "$themeMode • $accentColor",
                onClick = { showThemeDialog = true }
            )
            SettingsSwitchItem(
                icon = Icons.Rounded.FormatPaint,
                title = "Dynamic Color",
                subtitle = "Use Material You dynamic system colors",
                checked = dynamicColor,
                onCheckedChange = { viewModel.setDynamicColor(it) }
            )
            SettingsItem(
                icon = Icons.Rounded.ViewCompact,
                title = "UI Density",
                subtitle = uiDensity,
                onClick = { showUiDensityDialog = true }
            )
        }

        item {
            SettingsCategory("Playback")
            SettingsSwitchItem(
                icon = Icons.Rounded.SkipNext,
                title = "Gapless Playback",
                subtitle = "Smooth transition between tracks",
                checked = gaplessPlayback,
                onCheckedChange = { viewModel.setGaplessPlayback(it) }
            )
            SettingsItem(
                icon = Icons.Rounded.CompareArrows,
                title = "Crossfade",
                subtitle = crossfade,
                onClick = { showCrossfadeDialog = true }
            )
            SettingsSwitchItem(
                icon = Icons.Rounded.Headphones,
                title = "Pause on Unplug",
                subtitle = "Pause playback when headset is disconnected",
                checked = pauseOnUnplug,
                onCheckedChange = { viewModel.setPauseOnUnplug(it) }
            )
            SettingsItem(
                icon = Icons.Rounded.BugReport,
                title = "Playback Diagnostics",
                subtitle = "View ExoPlayer state, audio focus & media logs",
                onClick = { showDiagnosticsDialog = true }
            )
        }
        
        item {
            SettingsCategory("Music Providers")
            SettingsSwitchItem(
                icon = Icons.Rounded.Cloud,
                title = "Audius",
                subtitle = "Authorized streaming & search",
                checked = providerAudius,
                onCheckedChange = { viewModel.setProviderAudius(it) }
            )
            SettingsSwitchItem(
                icon = Icons.Rounded.MusicNote,
                title = "Jamendo",
                subtitle = "Free music & authorized playback",
                checked = providerJamendo,
                onCheckedChange = { viewModel.setProviderJamendo(it) }
            )
            SettingsSwitchItem(
                icon = Icons.Rounded.VideoLibrary,
                title = "YouTube Music",
                subtitle = "Metadata & discovery via ytmusicapi",
                checked = providerYtMusic,
                onCheckedChange = { viewModel.setProviderYtMusic(it) }
            )
            SettingsSwitchItem(
                icon = Icons.Rounded.Insights,
                title = "Last.fm",
                subtitle = "Discovery, charts & artist metadata",
                checked = providerLastFm,
                onCheckedChange = { viewModel.setProviderLastFm(it) }
            )
            SettingsSwitchItem(
                icon = Icons.Rounded.Subtitles,
                title = "LRCLIB",
                subtitle = "Synced & plain lyrics matching",
                checked = providerLrcLib,
                onCheckedChange = { viewModel.setProviderLrcLib(it) }
            )
        }

        item {
            SettingsCategory("Downloads")
            SettingsSwitchItem(
                icon = Icons.Rounded.Wifi,
                title = "Wi-Fi Only",
                subtitle = "Only download when connected to Wi-Fi",
                checked = wifiOnlyDownloads,
                onCheckedChange = { viewModel.setWifiOnlyDownloads(it) }
            )
            SettingsItem(
                icon = Icons.Rounded.Download,
                title = "Download Quality",
                subtitle = downloadQuality,
                onClick = { showDownloadQualityDialog = true }
            )
        }
        
        item {
            SettingsCategory("Notifications")
            SettingsSwitchItem(
                icon = Icons.Rounded.Notifications,
                title = "Playback Notifications",
                subtitle = "Show player in notification and lock screen",
                checked = playbackNotifications,
                onCheckedChange = { viewModel.setPlaybackNotifications(it) }
            )
            SettingsSwitchItem(
                icon = Icons.Rounded.Recommend,
                title = "New Recommendations",
                subtitle = "Get notified about new releases",
                checked = newRecommendations,
                onCheckedChange = { viewModel.setNewRecommendations(it) }
            )
        }

        item {
            SettingsCategory("Storage & Data")
            SettingsSwitchItem(
                icon = Icons.Rounded.WifiOff,
                title = "Offline Mode",
                subtitle = "Only show downloaded content",
                checked = offlineMode,
                onCheckedChange = { viewModel.setOfflineMode(it) }
            )
            SettingsItem(
                icon = Icons.Rounded.Storage,
                title = "Storage Management",
                subtitle = "Manage downloads and cache",
                onClick = onNavigateToStorage
            )
        }
        
        item {
            SettingsCategory("Accessibility")
            SettingsSwitchItem(
                icon = Icons.Rounded.TextFormat,
                title = "Large Text",
                subtitle = "Increase text size across the app",
                checked = largeText,
                onCheckedChange = { viewModel.setLargeText(it) }
            )
        }
        
        item {
            SettingsCategory("Privacy")
            SettingsItem(
                icon = Icons.Rounded.History,
                title = "Clear Listening History",
                subtitle = "Remove all recently played items and search history",
                onClick = { showClearHistoryDialog = true }
            )
        }
        
        item {
            SettingsCategory("About Aurora Music")
            
            val updateState by updateViewModel.updateState.collectAsStateWithLifecycle()
            val updateMode by updateViewModel.updateMode.collectAsStateWithLifecycle()

            val updateStatusSubtitle = when (val s = updateState) {
                is com.example.update.UpdateState.Checking -> "Checking for updates..."
                is com.example.update.UpdateState.NoUpdate -> "You're up to date (v${updateViewModel.currentVersionName})"
                is com.example.update.UpdateState.UpdateAvailable -> "Update Available (v${s.availableVersionName}) - Tap to install"
                is com.example.update.UpdateState.Downloading -> "Downloading update... ${s.progressPercent}%"
                is com.example.update.UpdateState.Downloaded -> "Update Downloaded! Tap to restart and install"
                is com.example.update.UpdateState.Installing -> "Installing update..."
                is com.example.update.UpdateState.Error -> s.message
                else -> "Check for official Google Play updates"
            }

            SettingsItem(
                icon = Icons.Rounded.SystemUpdate,
                title = "Check for Updates",
                subtitle = updateStatusSubtitle,
                onClick = {
                    val s = updateState
                    if (s is com.example.update.UpdateState.Downloaded) {
                        updateViewModel.completeUpdate()
                    } else {
                        updateViewModel.checkForUpdate(isUserInitiated = true)
                        showToast("Checking Google Play for updates...")
                    }
                }
            )

            SettingsItem(
                icon = Icons.Rounded.PublishedWithChanges,
                title = "Update Preference",
                subtitle = "Current: ${updateMode.name} Update",
                onClick = {
                    val nextMode = if (updateMode == com.example.update.UpdateMode.FLEXIBLE) com.example.update.UpdateMode.IMMEDIATE else com.example.update.UpdateMode.FLEXIBLE
                    updateViewModel.setUpdateMode(nextMode)
                    showToast("Update mode set to ${nextMode.name}")
                }
            )

            SettingsItem(
                icon = Icons.Rounded.PlayForWork,
                title = "Test Update Flow (Demo)",
                subtitle = "Simulate downloading flexible update & restart",
                onClick = {
                    updateViewModel.startSimulatedUpdate()
                    showToast("Simulating update download flow...")
                }
            )

            SettingsItem(
                icon = Icons.Rounded.Info,
                title = "Version",
                subtitle = "v${updateViewModel.currentVersionName} (Build ${updateViewModel.currentVersionCode})",
                onClick = {
                    if (!isDeveloperMode) {
                        developerModeUnlockClicks++
                        if (developerModeUnlockClicks >= 4) {
                            showToast("You are ${7 - developerModeUnlockClicks} steps away from being a developer.")
                        }
                        if (developerModeUnlockClicks == 7) {
                            showToast("You are now a developer!")
                        }
                    } else {
                        showToast("Developer mode is already enabled.")
                    }
                }
            )
            SettingsItem(
                icon = Icons.Rounded.Description,
                title = "Open Source Licenses",
                subtitle = "View software license notices",
                onClick = { showLicensesDialog = true }
            )
        }
        
        if (isDeveloperMode) {
            item {
                SettingsCategory("Developer Options")
                SettingsSwitchItem(
                    icon = Icons.Rounded.BugReport,
                    title = "Debug Logging",
                    subtitle = "Log network and audio events to logcat",
                    checked = debugLogging,
                    onCheckedChange = { viewModel.setDebugLogging(it) }
                )
                SettingsSwitchItem(
                    icon = Icons.Rounded.Dataset,
                    title = "Mock Data Mode",
                    subtitle = "Load sample data on start",
                    checked = mockDataMode,
                    onCheckedChange = { viewModel.setMockDataMode(it) }
                )
            }
        }
    }

    // --- DIALOGS ---

    // 1. Theme Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Appearance Settings") },
            text = {
                Column {
                    Text("Theme Mode", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(Spacing.S))
                    val modes = listOf("System Default", "Light Mode", "Dark Mode", "AMOLED Pitch Black")
                    Column(Modifier.selectableGroup()) {
                        modes.forEach { mode ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = (mode == themeMode),
                                        onClick = { viewModel.setThemeMode(mode) },
                                        role = Role.RadioButton
                                    )
                                    .padding(vertical = Spacing.S),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = (mode == themeMode), onClick = null)
                                Spacer(modifier = Modifier.width(Spacing.M))
                                Text(mode)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.L))
                    Text("Accent Color", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(Spacing.S))
                    val accents = listOf("Emerald Green", "Sky Blue", "Electric Purple", "Hot Pink", "Gold")
                    Column(Modifier.selectableGroup()) {
                        accents.forEach { colorName ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = (colorName == accentColor),
                                        onClick = { viewModel.setAccentColor(colorName) },
                                        role = Role.RadioButton
                                    )
                                    .padding(vertical = Spacing.S),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = (colorName == accentColor), onClick = null)
                                Spacer(modifier = Modifier.width(Spacing.M))
                                Text(colorName)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // 2. UI Density Dialog
    if (showUiDensityDialog) {
        AlertDialog(
            onDismissRequest = { showUiDensityDialog = false },
            title = { Text("UI Density") },
            text = {
                val densities = listOf(
                    "Compact" to "Condensed rows, tighter padding, compact items",
                    "Comfortable" to "Standard layout with optimal padding & spacing",
                    "Spacious" to "Generous margins, larger touch targets"
                )
                Column(Modifier.selectableGroup()) {
                    densities.forEach { (density, desc) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (density == uiDensity),
                                    onClick = {
                                        viewModel.setUiDensity(density)
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = Spacing.M),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = (density == uiDensity), onClick = null)
                            Spacer(modifier = Modifier.width(Spacing.M))
                            Column {
                                Text(density, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showUiDensityDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // 3. Crossfade Dialog
    if (showCrossfadeDialog) {
        AlertDialog(
            onDismissRequest = { showCrossfadeDialog = false },
            title = { Text("Crossfade Duration") },
            text = {
                val options = listOf("0s", "3s", "5s")
                Column(Modifier.selectableGroup()) {
                    options.forEach { opt ->
                        val labelText = if (opt == "0s") "0s (Off)" else opt
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (opt == crossfade),
                                    onClick = {
                                        viewModel.setCrossfade(opt)
                                        showCrossfadeDialog = false
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = Spacing.S),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = (opt == crossfade), onClick = null)
                            Spacer(modifier = Modifier.width(Spacing.M))
                            Text(labelText)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // 4. Audio Quality Dialog
    if (showAudioQualityDialog) {
        AlertDialog(
            onDismissRequest = { showAudioQualityDialog = false },
            title = { Text("Streaming Audio Quality") },
            text = {
                val options = listOf(
                    "Normal (160 kbps)" to "Uses less mobile data",
                    "High (256 kbps)" to "Recommended for high-fidelity audio",
                    "Very High (320 kbps)" to "Maximum clarity (uses more data)"
                )
                Column(Modifier.selectableGroup()) {
                    options.forEach { (opt, desc) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (opt == audioQuality),
                                    onClick = {
                                        viewModel.setAudioQuality(opt)
                                        showAudioQualityDialog = false
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = Spacing.M),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = (opt == audioQuality), onClick = null)
                            Spacer(modifier = Modifier.width(Spacing.M))
                            Column {
                                Text(opt, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // 5. Equalizer Dialog
    if (showEqualizerDialog) {
        AlertDialog(
            onDismissRequest = { showEqualizerDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Graphic Equalizer", fontWeight = FontWeight.Bold)
                    Switch(
                        checked = equalizerEnabled,
                        onCheckedChange = { viewModel.setEqualizerEnabled(it) }
                    )
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.M)
                ) {
                    item {
                        Text("Presets", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(Spacing.XS))
                        val presets = listOf("Flat", "Bass Boost", "Vocal", "Treble", "Rock", "Jazz", "Electronic")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.S)) {
                            items(presets.size) { idx ->
                                val p = presets[idx]
                                FilterChip(
                                    selected = (p == equalizerPreset),
                                    onClick = { viewModel.applyEqualizerPreset(p) },
                                    label = { Text(p) }
                                )
                            }
                        }
                    }

                    item {
                        Text("Frequency Bands (-12dB to +12dB)", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    }

                    // 60 Hz Band
                    item {
                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("60 Hz", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(String.format("%.1f dB", eqBand60Hz), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = eqBand60Hz,
                                onValueChange = { viewModel.setEqBand60Hz(it) },
                                valueRange = -12f..12f,
                                enabled = equalizerEnabled
                            )
                        }
                    }

                    // 230 Hz Band
                    item {
                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("230 Hz", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(String.format("%.1f dB", eqBand230Hz), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = eqBand230Hz,
                                onValueChange = { viewModel.setEqBand230Hz(it) },
                                valueRange = -12f..12f,
                                enabled = equalizerEnabled
                            )
                        }
                    }

                    // 910 Hz Band
                    item {
                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("910 Hz", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(String.format("%.1f dB", eqBand910Hz), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = eqBand910Hz,
                                onValueChange = { viewModel.setEqBand910Hz(it) },
                                valueRange = -12f..12f,
                                enabled = equalizerEnabled
                            )
                        }
                    }

                    // 4 kHz Band
                    item {
                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("4 kHz", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(String.format("%.1f dB", eqBand4kHz), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = eqBand4kHz,
                                onValueChange = { viewModel.setEqBand4kHz(it) },
                                valueRange = -12f..12f,
                                enabled = equalizerEnabled
                            )
                        }
                    }

                    // 14 kHz Band
                    item {
                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("14 kHz", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(String.format("%.1f dB", eqBand14kHz), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = eqBand14kHz,
                                onValueChange = { viewModel.setEqBand14kHz(it) },
                                valueRange = -12f..12f,
                                enabled = equalizerEnabled
                            )
                        }
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.S))
                        Text("Audio Enhancements", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    }

                    item {
                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Bass Boost", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("${eqBassBoost.toInt()}%", style = MaterialTheme.typography.bodySmall)
                            }
                            Slider(
                                value = eqBassBoost,
                                onValueChange = { viewModel.setEqBassBoost(it) },
                                valueRange = 0f..100f,
                                enabled = equalizerEnabled
                            )
                        }
                    }

                    item {
                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("3D Surround", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("${eq3dSurround.toInt()}%", style = MaterialTheme.typography.bodySmall)
                            }
                            Slider(
                                value = eq3dSurround,
                                onValueChange = { viewModel.setEq3dSurround(it) },
                                valueRange = 0f..100f,
                                enabled = equalizerEnabled
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEqualizerDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // 6. Download Quality Dialog
    if (showDownloadQualityDialog) {
        AlertDialog(
            onDismissRequest = { showDownloadQualityDialog = false },
            title = { Text("Download Quality") },
            text = {
                val options = listOf("Normal (160 kbps)", "High (256 kbps)", "Very High (320 kbps)")
                Column(Modifier.selectableGroup()) {
                    options.forEach { opt ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (opt == downloadQuality),
                                    onClick = {
                                        viewModel.setDownloadQuality(opt)
                                        showDownloadQualityDialog = false
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = Spacing.S),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = (opt == downloadQuality), onClick = null)
                            Spacer(modifier = Modifier.width(Spacing.M))
                            Text(opt)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // 7. Clear History Dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear Listening History?") },
            text = { Text("This will permanently remove all recently played tracks and search history from local storage.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.clearListeningHistory {
                        showClearHistoryDialog = false
                        showToast("Listening history cleared successfully.")
                    }
                }) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 8. Open Source Licenses Dialog
    if (showLicensesDialog) {
        AlertDialog(
            onDismissRequest = { showLicensesDialog = false },
            title = { Text("Open Source Licenses") },
            text = {
                Column {
                    Text("Aurora Music uses the following open source software:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(Spacing.M))
                    Text("• Jetpack Compose (Apache 2.0)")
                    Text("• Kotlin Coroutines & Flow (Apache 2.0)")
                    Text("• Room Database (Apache 2.0)")
                    Text("• Retrofit & OkHttp (Apache 2.0)")
                    Text("• Coil Image Loading (Apache 2.0)")
                    Text("• Innertube YouTube Music Engine")
                }
            },
            confirmButton = {
                TextButton(onClick = { showLicensesDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // 9. Playback & Developer Diagnostics Dialog
    if (showDiagnosticsDialog) {
        val diagSong by com.example.playback.AudioPlayerManager.currentSong.collectAsStateWithLifecycle()
        val diagIsPlaying by com.example.playback.AudioPlayerManager.isPlaying.collectAsStateWithLifecycle()
        val diagIsBuffering by com.example.playback.AudioPlayerManager.isBuffering.collectAsStateWithLifecycle()
        val diagPosMs by com.example.playback.AudioPlayerManager.positionMs.collectAsStateWithLifecycle()
        val diagDurMs by com.example.playback.AudioPlayerManager.durationMs.collectAsStateWithLifecycle()
        val diagStateName by com.example.playback.AudioPlayerManager.playerStateName.collectAsStateWithLifecycle()
        val diagAudioFocus by com.example.playback.AudioPlayerManager.audioFocusState.collectAsStateWithLifecycle()
        val diagVol by com.example.playback.AudioPlayerManager.volume.collectAsStateWithLifecycle()
        val diagErr by com.example.playback.AudioPlayerManager.errorMessage.collectAsStateWithLifecycle()
        val diagLog by com.example.playback.AudioPlayerManager.lastDiagnosticLog.collectAsStateWithLifecycle()

        AlertDialog(
            onDismissRequest = { showDiagnosticsDialog = false },
            title = { Text("Developer Diagnostics & Integration Test", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(Spacing.S)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(Spacing.M)) {
                            Text("FastAPI / ytmusicapi Backend Diagnostics", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(Spacing.XS))
                            Text("• Backend Base URL: ${com.example.data.api.AuroraApiClient.baseUrl}", style = MaterialTheme.typography.bodySmall)
                            Text("• Current Provider: ytmusicapi (YouTube Music)", style = MaterialTheme.typography.bodySmall)
                            Text("• Active Endpoints: /api/search, /api/charts, /api/songs, /api/artists, /api/albums, /api/playlists", style = MaterialTheme.typography.bodySmall)
                            Text("• Status: Integrated & Online", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.S))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(Spacing.M)) {
                            Text("Real-Time Player State", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(Spacing.XS))
                            Text("• Current Track: ${diagSong?.title ?: "None"}", style = MaterialTheme.typography.bodySmall)
                            Text("• Track ID: ${diagSong?.id ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
                            Text("• Artist: ${diagSong?.artist ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
                            Text("• Stream URL: ${diagSong?.streamUrl?.takeIf { it.isNotBlank() } ?: "None"}", style = MaterialTheme.typography.bodySmall)
                            Text("• Player State: $diagStateName", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text("• Is Playing: $diagIsPlaying | Buffering: $diagIsBuffering", style = MaterialTheme.typography.bodySmall)
                            Text("• Position / Duration: ${diagPosMs / 1000}s / ${diagDurMs / 1000}s", style = MaterialTheme.typography.bodySmall)
                            Text("• Volume: ${(diagVol * 100).toInt()}% | Audio Focus: $diagAudioFocus", style = MaterialTheme.typography.bodySmall)
                            if (diagErr != null) {
                                Text("• Last Error: $diagErr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.XS))
                    Text("Interactive Controls & Test Play", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.S)
                    ) {
                        Button(
                            onClick = {
                                com.example.playback.AudioPlayerManager.playTestTrack(context)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Track", style = MaterialTheme.typography.bodySmall)
                        }

                        Button(
                            onClick = {
                                com.example.playback.AudioPlayerManager.togglePlayPause()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(
                                if (diagIsPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (diagIsPlaying) "Pause" else "Resume", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.S)
                    ) {
                        OutlinedButton(
                            onClick = {
                                com.example.playback.AudioPlayerManager.seekTo((diagPosMs - 10000L).coerceAtLeast(0L))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("-10s", style = MaterialTheme.typography.bodySmall)
                        }

                        OutlinedButton(
                            onClick = {
                                com.example.playback.AudioPlayerManager.seekTo(diagPosMs + 10000L)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+10s", style = MaterialTheme.typography.bodySmall)
                        }

                        OutlinedButton(
                            onClick = {
                                com.example.playback.AudioPlayerManager.stop()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Stop", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.XS))
                    Text("Diagnostic Event Log:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text(diagLog, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = { showDiagnosticsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}


@Composable
fun SettingsCategory(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = Spacing.XL, vertical = Spacing.S)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = Spacing.XL, vertical = Spacing.M),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(Spacing.L))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = Spacing.XL, vertical = Spacing.M),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(Spacing.L))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
