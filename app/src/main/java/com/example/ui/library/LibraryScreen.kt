package com.example.ui.library

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.Song
import com.example.ui.components.QuickActionCard
import com.example.ui.components.SkeletonSongListItem
import com.example.ui.components.SongListItem
import com.example.ui.player.PlayerViewModel
import com.example.ui.state.UiState
import com.example.ui.theme.Spacing

@Composable
fun LibraryScreen(
    songsState: UiState<List<Song>>,
    onSongClick: (Song) -> Unit,
    onNavigateToDownloads: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel = viewModel()
) {
    val context = LocalContext.current
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showLocalFilesDialog by remember { mutableStateOf(false) }
    var filterFavoritesOnly by remember { mutableStateOf(false) }
    var playlistTitleInput by remember { mutableStateOf("") }

    fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(Spacing.XL)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Library",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row {
                        IconButton(onClick = onNavigateToSearch, modifier = Modifier.testTag("lib_search_btn")) {
                            Icon(Icons.Rounded.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { showCreatePlaylistDialog = true }, modifier = Modifier.testTag("lib_add_playlist_btn")) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add Playlist", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(Spacing.L))
                
                // Quick Actions
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.S)) {
                    QuickActionCard(
                        icon = Icons.Rounded.PlayArrow,
                        label = "Play All",
                        onClick = {
                            if (songsState is UiState.Success && songsState.data.isNotEmpty()) {
                                onSongClick(songsState.data.first())
                            } else {
                                showToast("No songs to play")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        icon = Icons.Rounded.Shuffle,
                        label = "Shuffle All",
                        onClick = {
                            if (songsState is UiState.Success && songsState.data.isNotEmpty()) {
                                onSongClick(songsState.data.random())
                            } else {
                                showToast("No songs to shuffle")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(Spacing.S))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.S)) {
                    QuickActionCard(
                        icon = if (filterFavoritesOnly) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        label = if (filterFavoritesOnly) "Showing Favs" else "Favorites",
                        onClick = {
                            filterFavoritesOnly = !filterFavoritesOnly
                            showToast(if (filterFavoritesOnly) "Filtered by Favorites" else "Showing All Songs")
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        icon = Icons.Rounded.Download,
                        label = "Downloads",
                        onClick = onNavigateToDownloads,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(Spacing.S))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.S)) {
                    QuickActionCard(
                        icon = Icons.Rounded.Folder,
                        label = "Local Files",
                        onClick = { showLocalFilesDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        icon = Icons.Rounded.History,
                        label = "History",
                        onClick = onNavigateToHistory,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        item {
            Text(
                text = if (filterFavoritesOnly) "Favorites" else "All Tracks",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = Spacing.XL, vertical = Spacing.S)
            )
        }
        
        when (songsState) {
            is UiState.Loading -> items(10) { SkeletonSongListItem() }
            is UiState.Success -> {
                val displayList = songsState.data
                if (displayList.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(Spacing.XL), contentAlignment = Alignment.Center) {
                            Text("Your library is empty.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(displayList) { song ->
                        SongListItem(song = song, onClick = { onSongClick(song) })
                    }
                }
            }
            is UiState.Empty -> item {
                Box(modifier = Modifier.fillMaxWidth().padding(Spacing.XL), contentAlignment = Alignment.Center) {
                    Text("Your library is empty.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            is UiState.Error -> item {
                Box(modifier = Modifier.fillMaxWidth().padding(Spacing.XL), contentAlignment = Alignment.Center) {
                    Text("Failed to load library.", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    // --- CREATE PLAYLIST DIALOG ---
    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text("Create Playlist") },
            text = {
                OutlinedTextField(
                    value = playlistTitleInput,
                    onValueChange = { playlistTitleInput = it },
                    label = { Text("Playlist Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (playlistTitleInput.isNotBlank()) {
                        playerViewModel.createPlaylist(playlistTitleInput) { playlist ->
                            showCreatePlaylistDialog = false
                            playlistTitleInput = ""
                            showToast("Created playlist '${playlist.title}'")
                        }
                    }
                }) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- LOCAL FILES DIALOG ---
    if (showLocalFilesDialog) {
        var localSongs by remember { mutableStateOf<List<Song>?>(null) }
        var isScanning by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            isScanning = true
            val repo = com.example.data.LocalMusicRepository(context)
            localSongs = repo.getLocalSongs()
            isScanning = false
        }

        AlertDialog(
            onDismissRequest = { showLocalFilesDialog = false },
            title = { Text("Local Device Music") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(Spacing.M))
                        Text(
                            "Scanning MediaStore audio files...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else {
                        val songs = localSongs ?: emptyList()
                        if (songs.isNotEmpty()) {
                            Text(
                                "Found ${songs.size} local tracks on device:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(Spacing.S))
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(Spacing.XS),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(songs) { song ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onSongClick(song)
                                                showLocalFilesDialog = false
                                            },
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(Spacing.M),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(Spacing.M))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(song.title, fontWeight = FontWeight.Bold, maxLines = 1)
                                                Text(song.artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                "No local MP3/audio files found in device storage.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(Spacing.S))
                            Text(
                                "You can test real audio playback using our authorized direct audio test track.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(Spacing.M))
                            Button(
                                onClick = {
                                    com.example.playback.AudioPlayerManager.playTestTrack(context)
                                    showLocalFilesDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(Spacing.S))
                                Text("Play Authorized Direct Test Track")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLocalFilesDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

