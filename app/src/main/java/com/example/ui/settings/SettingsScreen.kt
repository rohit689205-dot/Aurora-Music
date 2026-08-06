package com.example.ui.settings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.Spacing

@Composable
fun SettingsScreen(
    onNavigateToStorage: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    val dynamicColor by viewModel.dynamicColor.collectAsStateWithLifecycle()
    val gaplessPlayback by viewModel.gaplessPlayback.collectAsStateWithLifecycle()
    val pauseOnUnplug by viewModel.pauseOnUnplug.collectAsStateWithLifecycle()
    val wifiOnlyDownloads by viewModel.wifiOnlyDownloads.collectAsStateWithLifecycle()
    val offlineMode by viewModel.offlineMode.collectAsStateWithLifecycle()
    val playbackNotifications by viewModel.playbackNotifications.collectAsStateWithLifecycle()
    val newRecommendations by viewModel.newRecommendations.collectAsStateWithLifecycle()
    val largeText by viewModel.largeText.collectAsStateWithLifecycle()

    var developerModeUnlockClicks by remember { mutableIntStateOf(0) }
    val isDeveloperMode = developerModeUnlockClicks >= 7
    val context = LocalContext.current

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
                subtitle = "System Default",
                onClick = { showToast("Theme selection not implemented") }
            )
            SettingsSwitchItem(
                icon = Icons.Rounded.FormatPaint,
                title = "Dynamic Color",
                subtitle = "Use Material You colors",
                checked = dynamicColor,
                onCheckedChange = { viewModel.setDynamicColor(it) }
            )
            SettingsItem(
                icon = Icons.Rounded.ViewCompact,
                title = "UI Density",
                subtitle = "Comfortable",
                onClick = { showToast("UI Density settings not implemented") }
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
                subtitle = "Off",
                onClick = { showToast("Crossfade settings not implemented") }
            )
            SettingsSwitchItem(
                icon = Icons.Rounded.Headphones,
                title = "Pause on Unplug",
                subtitle = "Pause playback when headset is disconnected",
                checked = pauseOnUnplug,
                onCheckedChange = { viewModel.setPauseOnUnplug(it) }
            )
        }
        
        item {
            SettingsCategory("Audio")
            SettingsItem(
                icon = Icons.Rounded.HighQuality,
                title = "Audio Quality",
                subtitle = "High",
                onClick = { showToast("Audio Quality settings not implemented") }
            )
            SettingsItem(
                icon = Icons.Rounded.Equalizer,
                title = "Equalizer",
                subtitle = "Adjust audio settings",
                onClick = { showToast("Equalizer not implemented") }
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
                subtitle = "High",
                onClick = { showToast("Download Quality settings not implemented") }
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
                subtitle = "Remove all recently played items",
                onClick = { showToast("History cleared") }
            )
        }
        
        item {
            SettingsCategory("About")
            SettingsItem(
                icon = Icons.Rounded.Info,
                title = "Version",
                subtitle = "1.0.0",
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
                onClick = { showToast("Licenses not implemented") }
            )
        }
        
        if (isDeveloperMode) {
            item {
                SettingsCategory("Developer Options")
                SettingsItem(
                    icon = Icons.Rounded.BugReport,
                    title = "Debug Logging",
                    subtitle = "Enabled",
                    onClick = { showToast("Toggled debug logging") }
                )
                SettingsItem(
                    icon = Icons.Rounded.Dataset,
                    title = "Mock Data Mode",
                    subtitle = "Load sample data on start",
                    onClick = { showToast("Toggled mock data") }
                )
            }
        }
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
