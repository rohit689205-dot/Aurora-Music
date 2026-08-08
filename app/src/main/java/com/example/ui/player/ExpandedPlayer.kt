package com.example.ui.player

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.model.Song
import com.example.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedPlayer(
    song: Song,
    isPlaying: Boolean,
    progress: Float,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClose: () -> Unit,
    onQueueClick: () -> Unit,
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel = viewModel()
) {
    val context = LocalContext.current
    val isFavorite by playerViewModel.isCurrentFavorite.collectAsStateWithLifecycle()
    val isShuffle by playerViewModel.isShuffleEnabled.collectAsStateWithLifecycle()
    val repeatMode by playerViewModel.repeatMode.collectAsStateWithLifecycle()
    val userPlaylists by playerViewModel.userPlaylists.collectAsStateWithLifecycle()
    val errorMessage by playerViewModel.errorMessage.collectAsStateWithLifecycle()

    var showPlaylistSheet by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showPlayerMenuSheet by remember { mutableStateOf(false) }
    var newPlaylistTitle by remember { mutableStateOf("") }

    fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun shareTrack() {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Listening to '${song.title}' by ${song.artist} on Aurora Music!")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Track")
        context.startActivity(shareIntent)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .systemBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose, modifier = Modifier.testTag("close_player_btn")) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Close player",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Now Playing",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { showPlayerMenuSheet = true }, modifier = Modifier.testTag("player_menu_btn")) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cover Image
            AsyncImage(
                model = coil.request.ImageRequest.Builder(LocalContext.current)
                    .data(song.artworkUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Album Cover",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Title & Favorite
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = { playerViewModel.toggleFavoriteCurrentSong() },
                    modifier = Modifier.testTag("player_favorite_btn")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val is451 = errorMessage?.contains("region") == true || errorMessage?.contains("451") == true
                            if (is451) {
                                Button(
                                    onClick = onNext,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Choose Another Track")
                                }
                            } else {
                                Button(
                                    onClick = onNext,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Try Another Track")
                                }
                                OutlinedButton(
                                    onClick = { playerViewModel.playSong(song) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Actions Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(
                    onClick = {
                        playerViewModel.downloadCurrentSong {
                            showToast("Downloaded '${song.title}' to offline storage.")
                        }
                    },
                    modifier = Modifier.testTag("player_download_btn")
                ) {
                    Icon(imageVector = Icons.Rounded.Download, contentDescription = "Download", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(
                    onClick = { showPlaylistSheet = true },
                    modifier = Modifier.testTag("player_add_playlist_btn")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Rounded.PlaylistAdd, contentDescription = "Add to playlist", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(
                    onClick = { shareTrack() },
                    modifier = Modifier.testTag("player_share_btn")
                ) {
                    Icon(imageVector = Icons.Rounded.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Progress Slider
            Slider(
                value = progress,
                onValueChange = { playerViewModel.seekToProgress(it) },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth().testTag("player_progress_slider")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val currentMs = (song.duration * progress).toLong()
                val currentMinutes = (currentMs / 1000) / 60
                val currentSeconds = (currentMs / 1000) % 60
                Text("${currentMinutes}:${currentSeconds.toString().padStart(2, '0')}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val minutes = (song.duration / 1000) / 60
                val seconds = (song.duration / 1000) % 60
                Text("${minutes}:${seconds.toString().padStart(2, '0')}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Main Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { playerViewModel.toggleShuffle() },
                    modifier = Modifier.testTag("player_shuffle_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onPrevious, modifier = Modifier.size(64.dp).testTag("player_prev_btn")) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(48.dp)
                    )
                }
                IconButton(onClick = onPlayPause, modifier = Modifier.size(80.dp).testTag("player_play_pause_btn")) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.PauseCircleFilled else Icons.Rounded.PlayCircleFilled,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(80.dp)
                    )
                }
                IconButton(onClick = onNext, modifier = Modifier.size(64.dp).testTag("player_next_btn")) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(48.dp)
                    )
                }
                IconButton(
                    onClick = { playerViewModel.toggleRepeat() },
                    modifier = Modifier.testTag("player_repeat_btn")
                ) {
                    Icon(
                        imageVector = when (repeatMode) {
                            2 -> Icons.Rounded.RepeatOne
                            else -> Icons.Rounded.Repeat
                        },
                        contentDescription = "Repeat Mode",
                        tint = if (repeatMode > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Audio & Queue Options
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showToast("Audio FX Equalizer active") },
                    modifier = Modifier.testTag("player_speaker_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Speaker,
                        contentDescription = "Audio options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onQueueClick, modifier = Modifier.testTag("player_queue_btn")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                        contentDescription = "Queue",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // --- PLAYLIST PICKER BOTTOM SHEET ---
        if (showPlaylistSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPlaylistSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.XL)
                ) {
                    Text(
                        text = "Add to Playlist",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(Spacing.L))

                    OutlinedButton(
                        onClick = {
                            showPlaylistSheet = false
                            showCreatePlaylistDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(Spacing.S))
                        Text("Create New Playlist")
                    }

                    Spacer(modifier = Modifier.height(Spacing.M))

                    if (userPlaylists.isEmpty()) {
                        Text(
                            text = "No playlists created yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = Spacing.L)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                            items(userPlaylists) { playlist ->
                                ListItem(
                                    headlineContent = { Text(playlist.title) },
                                    leadingContent = {
                                        Icon(Icons.AutoMirrored.Rounded.PlaylistPlay, contentDescription = null)
                                    },
                                    modifier = Modifier.clickable {
                                        playerViewModel.addSongToPlaylist(playlist.id, song) {
                                            showPlaylistSheet = false
                                            showToast("Added '${song.title}' to '${playlist.title}'")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- CREATE PLAYLIST DIALOG ---
        if (showCreatePlaylistDialog) {
            AlertDialog(
                onDismissRequest = { showCreatePlaylistDialog = false },
                title = { Text("New Playlist") },
                text = {
                    OutlinedTextField(
                        value = newPlaylistTitle,
                        onValueChange = { newPlaylistTitle = it },
                        label = { Text("Playlist Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newPlaylistTitle.isNotBlank()) {
                                playerViewModel.createPlaylist(newPlaylistTitle) { playlist ->
                                    playerViewModel.addSongToPlaylist(playlist.id, song) {
                                        showCreatePlaylistDialog = false
                                        newPlaylistTitle = ""
                                        showToast("Created '${playlist.title}' & added track.")
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Create & Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreatePlaylistDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // --- PLAYER MENU BOTTOM SHEET ---
        if (showPlayerMenuSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPlayerMenuSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Spacing.XXL)
                ) {
                    ListItem(
                        headlineContent = { Text("Share Track") },
                        leadingContent = { Icon(Icons.Rounded.Share, contentDescription = null) },
                        modifier = Modifier.clickable {
                            showPlayerMenuSheet = false
                            shareTrack()
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Add to Playlist") },
                        leadingContent = { Icon(Icons.AutoMirrored.Rounded.PlaylistAdd, contentDescription = null) },
                        modifier = Modifier.clickable {
                            showPlayerMenuSheet = false
                            showPlaylistSheet = true
                        }
                    )
                    ListItem(
                        headlineContent = { Text(if (isFavorite) "Remove from Favorites" else "Add to Favorites") },
                        leadingContent = { Icon(if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, contentDescription = null) },
                        modifier = Modifier.clickable {
                            showPlayerMenuSheet = false
                            playerViewModel.toggleFavoriteCurrentSong()
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Download Track") },
                        leadingContent = { Icon(Icons.Rounded.Download, contentDescription = null) },
                        modifier = Modifier.clickable {
                            showPlayerMenuSheet = false
                            playerViewModel.downloadCurrentSong {
                                showToast("Downloaded '${song.title}'")
                            }
                        }
                    )
                }
            }
        }
    }
}

