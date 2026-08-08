package com.example.update

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Spacing

@Composable
fun AuroraUpdateOverlay(
    updateViewModel: UpdateViewModel,
    modifier: Modifier = Modifier
) {
    val updateState by updateViewModel.updateState.collectAsState()
    val updateMode by updateViewModel.updateMode.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    var dismissFlexiblePrompt by remember { mutableStateOf(false) }

    when (val state = updateState) {
        is UpdateState.UpdateAvailable -> {
            if (!dismissFlexiblePrompt) {
                AlertDialog(
                    onDismissRequest = { dismissFlexiblePrompt = true },
                    shape = RoundedCornerShape(24.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.SystemUpdate,
                            contentDescription = "System Update",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    },
                    title = {
                        Text(
                            text = if (state.updateMode == UpdateMode.IMMEDIATE) "Critical Update Required" else "New Version Available",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
                            Text(
                                text = "Aurora Music v${state.availableVersionName} is available.",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Update Aurora Music to get the latest features, audio engine improvements, and performance fixes.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(Spacing.XS))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Notes: ${state.releaseNotes}",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(Spacing.M)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                activity?.let { updateViewModel.startUpdateFlow(it) }
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("btn_update_now")
                        ) {
                            Text("Update Now")
                        }
                    },
                    dismissButton = {
                        if (state.updateMode == UpdateMode.FLEXIBLE) {
                            TextButton(
                                onClick = { dismissFlexiblePrompt = true },
                                modifier = Modifier.testTag("btn_update_later")
                            ) {
                                Text("Later")
                            }
                        }
                    }
                )
            }
        }

        is UpdateState.Downloading -> {
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.L, vertical = Spacing.M)
                    .testTag("overlay_downloading_update"),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                ),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.M),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        progress = { state.progressPercent / 100f },
                        modifier = Modifier.size(36.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.width(Spacing.M))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Downloading Update... ${state.progressPercent}%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val downloadedMb = state.bytesDownloaded / (1024f * 1024f)
                        val totalMb = state.totalBytesToDownload / (1024f * 1024f)
                        Text(
                            text = String.format("%.1f MB / %.1f MB", downloadedMb, totalMb),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(Spacing.XS))
                        LinearProgressIndicator(
                            progress = { state.progressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }

        is UpdateState.Downloaded -> {
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.L, vertical = Spacing.M)
                    .testTag("overlay_update_downloaded"),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.M),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DownloadDone,
                            contentDescription = "Update Ready",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.M))
                        Column {
                            Text(
                                text = "Update Ready",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Restart Aurora Music to install the update.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Button(
                        onClick = { updateViewModel.completeUpdate() },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("btn_restart_now")
                    ) {
                        Text("Restart Now")
                    }
                }
            }
        }

        else -> {}
    }
}

/**
 * Reusable UpdateDialog component in Compose that supports both immediate and flexible update states,
 * utilizing the AppUpdateManager state flow from UpdateViewModel to drive the UI.
 * Includes progress bar support for flexible updates.
 */
@Composable
fun UpdateDialog(
    updateViewModel: UpdateViewModel,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    AuroraUpdateOverlay(
        updateViewModel = updateViewModel,
        modifier = modifier
    )
}

