package com.example.ui.settings

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToStorage: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offlineMode by remember { mutableStateOf(false) }
    var dynamicColor by remember { mutableStateOf(true) }
    var gaplessPlayback by remember { mutableStateOf(true) }
    var pauseOnUnplug by remember { mutableStateOf(true) }
    var wifiOnlyDownloads by remember { mutableStateOf(false) }
    var developerModeUnlockClicks by remember { mutableIntStateOf(0) }
    val isDeveloperMode = developerModeUnlockClicks >= 7

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(Spacing.XL)) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        item {
            SettingsCategory("Appearance")
            SettingsItem(
                icon = Icons.Rounded.Palette,
                title = "Theme",
                subtitle = "System Default",
                onClick = { /* TODO */ }
            )
            SettingsSwitchItem(
                icon = Icons.Rounded.FormatPaint,
                title = "Dynamic Color",
                subtitle = "Use Material You colors",
                checked = dynamicColor,
                onCheckedChange = { dynamicColor = it }
            )
            SettingsItem(
                icon = Icons.Rounded.ViewCompact,
                title = "UI Density",
                subtitle = "Comfortable",
                onClick = { /* TODO */ }
            )
        }

        item {
            SettingsCategory("Playback")
            SettingsSwitchItem(
                icon = Icons.Rounded.SkipNext,
                title = "Gapless Playback",
                subtitle = "Smooth transition between tracks",
                checked = gaplessPlayback,
                onCheckedChange = { gaplessPlayback = it }
            )
            SettingsItem(
                icon = Icons.Rounded.CompareArrows,
                title = "Crossfade",
                subtitle = "Off",
                onClick = { /* TODO */ }
            )
            SettingsSwitchItem(
                icon = Icons.Rounded.Headphones,
                title = "Pause on Unplug",
                subtitle = "Pause playback when headset is disconnected",
                checked = pauseOnUnplug,
                onCheckedChange = { pauseOnUnplug = it }
            )
        }
        
        item {
            SettingsCategory("Audio")
            SettingsItem(
                icon = Icons.Rounded.HighQuality,
                title = "Audio Quality",
                subtitle = "High",
                onClick = { /* TODO */ }
            )
            SettingsItem(
                icon = Icons.Rounded.Equalizer,
                title = "Equalizer",
                subtitle = "Adjust audio settings",
                onClick = { /* TODO */ }
            )
        }

        item {
            SettingsCategory("Downloads")
            SettingsSwitchItem(
                icon = Icons.Rounded.Wifi,
                title = "Wi-Fi Only",
                subtitle = "Only download when connected to Wi-Fi",
                checked = wifiOnlyDownloads,
                onCheckedChange = { wifiOnlyDownloads = it }
            )
            SettingsItem(
                icon = Icons.Rounded.Download,
                title = "Download Quality",
                subtitle = "High",
                onClick = { /* TODO */ }
            )
        }
        
        item {
            SettingsCategory("Notifications")
            SettingsSwitchItem(
                icon = Icons.Rounded.Notifications,
                title = "Playback Notifications",
                subtitle = "Show player in notification and lock screen",
                checked = true,
                onCheckedChange = { /* TODO */ }
            )
            SettingsSwitchItem(
                icon = Icons.Rounded.Recommend,
                title = "New Recommendations",
                subtitle = "Get notified about new releases",
                checked = false,
                onCheckedChange = { /* TODO */ }
            )
        }

        item {
            SettingsCategory("Storage & Data")
            SettingsSwitchItem(
                icon = Icons.Rounded.WifiOff,
                title = "Offline Mode",
                subtitle = "Only show downloaded content",
                checked = offlineMode,
                onCheckedChange = { offlineMode = it }
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
                checked = false,
                onCheckedChange = { /* TODO */ }
            )
        }
        
        item {
            SettingsCategory("Privacy")
            SettingsItem(
                icon = Icons.Rounded.History,
                title = "Clear Listening History",
                subtitle = "Remove all recently played items",
                onClick = { /* TODO */ }
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
                    }
                }
            )
            SettingsItem(
                icon = Icons.Rounded.Description,
                title = "Open Source Licenses",
                onClick = { /* TODO */ }
            )
        }
        
        if (isDeveloperMode) {
            item {
                SettingsCategory("Developer Options")
                SettingsItem(
                    icon = Icons.Rounded.BugReport,
                    title = "Debug Logging",
                    subtitle = "Enabled",
                    onClick = { /* TODO */ }
                )
                SettingsItem(
                    icon = Icons.Rounded.Dataset,
                    title = "Mock Data Mode",
                    subtitle = "Load sample data on start",
                    onClick = { /* TODO */ }
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
