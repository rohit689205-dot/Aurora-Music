package com.example.ui.playlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.model.Song
import com.example.ui.components.SkeletonSongListItem
import com.example.ui.search.SongListItem
import com.example.ui.state.UiState
import com.example.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    playlistId: String = "trending_playlist",
    onBack: () -> Unit,
    onSongClick: (Song) -> Unit = {},
    viewModel: PlaylistViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val playlistState by viewModel.playlistState.collectAsStateWithLifecycle()

    LaunchedEffect(playlistId) {
        viewModel.loadPlaylist(playlistId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Playlist", maxLines = 1, overflow = TextOverflow.Ellipsis) },
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
            when (val state = playlistState) {
                is UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    items(5) { SkeletonSongListItem() }
                }
                is UiState.Error -> {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.XL)
                        ) {
                            Column(
                                modifier = Modifier.padding(Spacing.XL),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Playlist Error",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(Spacing.S))
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(Spacing.L))
                                Button(
                                    onClick = { viewModel.retry() },
                                    modifier = Modifier.testTag("playlist_retry_btn")
                                ) {
                                    Icon(Icons.Rounded.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.width(Spacing.S))
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
                is UiState.Empty -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.XXL),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No tracks found in this playlist.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                is UiState.Success -> {
                    val playlistData = state.data
                    val playlist = playlistData.playlist
                    val items = playlistData.items

                    // Header
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.XL),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                AsyncImage(
                                    model = playlist.artwork,
                                    contentDescription = playlist.title,
                                    modifier = Modifier
                                        .size(180.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(Spacing.L))
                            Text(
                                text = playlist.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(Spacing.XS))
                            Text(
                                text = "By ${playlist.owner} • ${items.size} tracks",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!playlist.description.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(Spacing.S))
                                Text(
                                    text = playlist.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(Spacing.L))
                            Button(
                                onClick = { if (items.isNotEmpty()) onSongClick(items.first()) },
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .height(48.dp)
                                    .testTag("play_playlist_btn")
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(Spacing.S))
                                Text("Play All Tracks")
                            }
                        }
                    }

                    // Songs
                    items(items) { song ->
                        SongListItem(
                            song = song,
                            onClick = { onSongClick(song) },
                            onInfoClick = { onSongClick(song) }
                        )
                    }
                }
            }
        }
    }
}
