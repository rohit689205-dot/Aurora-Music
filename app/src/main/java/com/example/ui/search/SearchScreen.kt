package com.example.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.api.YouTubeMapper
import com.example.model.Artist
import com.example.model.Playlist
import com.example.model.Song
import com.example.ui.components.SkeletonSongListItem
import com.example.ui.state.UiState
import com.example.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onSongSelect: (Song) -> Unit = {},
    onPlaylistSelect: (String) -> Unit = {},
    onArtistSelect: (String) -> Unit = {},
    viewModel: YouTubeSearchViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()

    var selectedSongForDetails by remember { mutableStateOf<Song?>(null) }
    val defaultSuggestions = listOf("Synthwave Music", "Lofi Hip Hop Beats", "Top Popular Songs 2026", "Chillout Playlist", "EDM Mix")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = query,
                        onValueChange = { viewModel.onQueryChanged(it) },
                        placeholder = { Text("Search YouTube videos, songs, playlists...", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = Spacing.M)
                            .testTag("youtube_search_input"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.onQueryChanged("") },
                                    modifier = Modifier.testTag("clear_search_btn")
                                ) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Clear")
                                }
                            }
                        }
                    )
                },
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter Chips Bar
            LazyRow(
                contentPadding = PaddingValues(horizontal = Spacing.XL, vertical = Spacing.S),
                horizontalArrangement = Arrangement.spacedBy(Spacing.M)
            ) {
                items(SearchFilter.entries.toTypedArray()) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { viewModel.onFilterSelected(filter) },
                        label = { Text(filter.label) },
                        leadingIcon = if (selectedFilter == filter) {
                            { Icon(Icons.Rounded.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        modifier = Modifier.testTag("filter_${filter.name.lowercase()}")
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                if (query.isBlank()) {
                    item {
                        Text(
                            text = "Popular YouTube Suggestions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = Spacing.XL, vertical = Spacing.M)
                        )
                    }
                    items(defaultSuggestions) { suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.onQueryChanged(suggestion) }
                                .padding(horizontal = Spacing.XL, vertical = Spacing.M),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(Spacing.L))
                            Text(text = suggestion, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                } else {
                    when (val state = searchState) {
                        is UiState.Loading -> {
                            items(6) { SkeletonSongListItem() }
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
                                            text = "YouTube Data API Error",
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
                                            modifier = Modifier.testTag("retry_search_btn")
                                        ) {
                                            Icon(Icons.Rounded.Refresh, contentDescription = null)
                                            Spacer(modifier = Modifier.width(Spacing.S))
                                            Text("Retry Request")
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
                                    Text(
                                        text = "No YouTube results found for \"$query\"",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        is UiState.Success -> {
                            val data = state.data

                            // Artists / Channels section
                            if (data.artists.isNotEmpty() && (selectedFilter == SearchFilter.ALL || selectedFilter == SearchFilter.ARTISTS)) {
                                item {
                                    Text(
                                        text = "Channels & Artists",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = Spacing.XL, vertical = Spacing.M)
                                    )
                                }
                                items(data.artists) { artist ->
                                    YouTubeArtistListItem(artist = artist, onClick = { onArtistSelect(artist.id) })
                                }
                            }

                            // Playlists section
                            if (data.playlists.isNotEmpty() && (selectedFilter == SearchFilter.ALL || selectedFilter == SearchFilter.PLAYLISTS)) {
                                item {
                                    Text(
                                        text = "Playlists",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = Spacing.XL, vertical = Spacing.M)
                                    )
                                }
                                items(data.playlists) { playlist ->
                                    YouTubePlaylistListItem(playlist = playlist, onClick = { onPlaylistSelect(playlist.id) })
                                }
                            }

                            // Videos / Songs section
                            if (data.songs.isNotEmpty() && (selectedFilter == SearchFilter.ALL || selectedFilter == SearchFilter.MUSIC)) {
                                item {
                                    Text(
                                        text = "Videos & Music Tracks",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = Spacing.XL, vertical = Spacing.M)
                                    )
                                }
                                items(data.songs) { song ->
                                    YouTubeSongListItem(
                                        song = song,
                                        onClick = { onSongSelect(song) },
                                        onInfoClick = { selectedSongForDetails = song }
                                    )
                                }
                            }

                            // Load More / Pagination
                            if (!data.nextPageToken.isNullOrBlank()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(Spacing.XL),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isLoadingMore) {
                                            CircularProgressIndicator(modifier = Modifier.size(28.dp))
                                        } else {
                                            OutlinedButton(
                                                onClick = { viewModel.loadNextPage() },
                                                modifier = Modifier.testTag("load_more_btn")
                                            ) {
                                                Text("Load More Results")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Video Detail Modal Dialog
    selectedSongForDetails?.let { song ->
        AlertDialog(
            onDismissRequest = { selectedSongForDetails = null },
            title = { Text(text = song.title, maxLines = 2, overflow = TextOverflow.Ellipsis) },
            text = {
                Column {
                    AsyncImage(
                        model = song.artworkUrl,
                        contentDescription = song.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(Spacing.M))
                    Text("Channel: ${song.artist}", fontWeight = FontWeight.SemiBold)
                    Text("YouTube Video ID: ${song.id}")
                    if (song.duration > 0) {
                        val minutes = (song.duration / 1000) / 60
                        val seconds = (song.duration / 1000) % 60
                        Text("Duration: ${minutes}m ${seconds}s")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onSongSelect(song)
                    selectedSongForDetails = null
                }) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(Spacing.S))
                    Text("Play Track")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedSongForDetails = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun YouTubeSongListItem(
    song: Song,
    onClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.XL, vertical = Spacing.S),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            AsyncImage(
                model = song.artworkUrl,
                contentDescription = song.title,
                modifier = Modifier
                    .size(60.dp, 50.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Surface(
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(2.dp)
            ) {
                val secTotal = song.duration / 1000
                val mins = secTotal / 60
                val secs = secTotal % 60
                Text(
                    text = String.format("%d:%02d", mins, secs),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(Spacing.M))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onInfoClick) {
            Icon(Icons.Rounded.Info, contentDescription = "Details", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onClick) {
            Icon(Icons.Rounded.PlayArrow, contentDescription = "Play", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun YouTubePlaylistListItem(
    playlist: Playlist,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.XL, vertical = Spacing.S),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            AsyncImage(
                model = playlist.artwork,
                contentDescription = playlist.title,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Icon(
                Icons.Rounded.PlaylistPlay,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(24.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            )
        }
        Spacer(modifier = Modifier.width(Spacing.M))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Playlist • ${playlist.owner}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun YouTubeArtistListItem(
    artist: Artist,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.XL, vertical = Spacing.S),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = artist.image,
            contentDescription = artist.name,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(Spacing.M))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (artist.verified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = "Verified",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = "YouTube Channel",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
