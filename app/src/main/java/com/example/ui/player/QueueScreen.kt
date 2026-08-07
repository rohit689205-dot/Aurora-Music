package com.example.ui.player

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.model.Song
import com.example.ui.components.SongListItem
import com.example.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    currentSong: Song?,
    queue: List<Song>,
    onBack: () -> Unit,
    onSongClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Playing Queue") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Text(
                    text = "Now Playing",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = Spacing.XL, vertical = Spacing.M)
                )
            }
            if (currentSong != null) {
                item {
                    SongListItem(
                        song = currentSong,
                        onClick = { onSongClick(currentSong) },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    )
                }
            }
            
            item {
                Text(
                    text = "Next In Queue",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Spacing.XL, vertical = Spacing.M)
                )
            }
            
            itemsIndexed(queue) { index, song ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        SongListItem(
                            song = song,
                            onClick = { onSongClick(song) }
                        )
                    }
                    var showTrackMenu by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { showTrackMenu = true },
                        modifier = Modifier.padding(end = Spacing.L)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DragHandle,
                            contentDescription = "Queue track options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        DropdownMenu(
                            expanded = showTrackMenu,
                            onDismissRequest = { showTrackMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Play Next") },
                                onClick = {
                                    showTrackMenu = false
                                    onSongClick(song)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Remove from Queue") },
                                onClick = {
                                    showTrackMenu = false
                                    showToast("Removed '${song.title}' from queue")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
