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
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Refresh
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
import com.example.model.Album
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
    onAlbumSelect: (String) -> Unit = {},
    viewModel: SearchViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    val speechLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)?.getOrNull(0)
            if (!spokenText.isNullOrEmpty()) {
                viewModel.onQueryChanged(spokenText)
            }
        }
    }

    var selectedSongForDetails by remember { mutableStateOf<Song?>(null) }
    val defaultSuggestions = listOf("Top Tracks 2026", "Arijit Singh", "Lofi Beats", "Synthwave Hits", "Chillout Playlist")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = query,
                        onValueChange = { viewModel.onQueryChanged(it) },
                        placeholder = { Text("Search songs, artists, albums...", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = Spacing.M)
                            .testTag("music_search_input"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (query.isNotEmpty()) {
                                    IconButton(
                                        onClick = { viewModel.onQueryChanged("") },
                                        modifier = Modifier.testTag("clear_search_btn")
                                    ) {
                                        Icon(Icons.Rounded.Close, contentDescription = "Clear")
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                            putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Speak song or artist name...")
                                        }
                                        try {
                                            speechLauncher.launch(intent)
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(context, "Voice input not supported on this device", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.testTag("search_mic_btn")
                                ) {
                                    Icon(
                                        Icons.Rounded.Mic,
                                        contentDescription = "Voice Search",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
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
                            text = "Popular Music Searches",
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
                                            text = "Search Error",
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
                                        text = "No results found for \"$query\"",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        is UiState.Success -> {
                            val data = state.data

                            // Artists section
                            if (data.artists.isNotEmpty() && (selectedFilter == SearchFilter.ALL || selectedFilter == SearchFilter.ARTISTS)) {
                                item {
                                    Text(
                                        text = "Artists",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = Spacing.XL, vertical = Spacing.M)
                                    )
                                }
                                items(data.artists) { artist ->
                                    ArtistListItem(artist = artist, onClick = { onArtistSelect(artist.id) })
                                }
                            }

                            // Albums section
                            if (data.albums.isNotEmpty() && (selectedFilter == SearchFilter.ALL || selectedFilter == SearchFilter.ALBUMS)) {
                                item {
                                    Text(
                                        text = "Albums",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = Spacing.XL, vertical = Spacing.M)
                                    )
                                }
                                items(data.albums) { album ->
                                    AlbumListItem(album = album, onClick = { onAlbumSelect(album.id) })
                                }
                            }

                            // Tracks section
                            if (data.songs.isNotEmpty() && (selectedFilter == SearchFilter.ALL || selectedFilter == SearchFilter.SONGS)) {
                                item {
                                    Text(
                                        text = "Tracks",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = Spacing.XL, vertical = Spacing.M)
                                    )
                                }
                                items(data.songs) { song ->
                                    SongListItem(
                                        song = song,
                                        onClick = { onSongSelect(song) },
                                        onInfoClick = { selectedSongForDetails = song }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Track Detail Modal Dialog
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
                    Text("Artist: ${song.artist}", fontWeight = FontWeight.SemiBold)
                    Text("Album: ${song.album}")
                    Text("Track ID: ${song.id}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
fun SongListItem(
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
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
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
                text = "${song.artist} • ${song.album}",
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
fun AlbumListItem(
    album: Album,
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
                model = album.artwork,
                contentDescription = album.title,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Icon(
                Icons.Rounded.Album,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(24.dp)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
            )
        }
        Spacer(modifier = Modifier.width(Spacing.M))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = album.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Album • ${album.totalTracks} tracks",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PlaylistListItem(
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
fun ArtistListItem(
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
                text = "Artist",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
