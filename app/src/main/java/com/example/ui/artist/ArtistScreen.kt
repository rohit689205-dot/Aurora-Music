package com.example.ui.artist

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
import com.example.ui.search.AlbumListItem
import com.example.ui.search.SongListItem
import com.example.ui.state.UiState
import com.example.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistScreen(
    channelIdOrName: String = "m83",
    onBack: () -> Unit,
    onSongClick: (Song) -> Unit = {},
    onAlbumClick: (String) -> Unit = {},
    viewModel: ArtistViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val artistState by viewModel.artistState.collectAsStateWithLifecycle()

    LaunchedEffect(channelIdOrName) {
        viewModel.loadArtist(channelIdOrName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Artist Details", maxLines = 1, overflow = TextOverflow.Ellipsis) },
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
            when (val state = artistState) {
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
                                    text = "Artist Details Error",
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
                                    modifier = Modifier.testTag("artist_retry_btn")
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
                            Text("Artist details not found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                is UiState.Success -> {
                    val data = state.data
                    val artist = data.artist

                    // Profile Header
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.XL),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = artist.image,
                                contentDescription = artist.name,
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(Spacing.M))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = artist.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                if (artist.verified) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        Icons.Rounded.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            if (artist.followers > 0) {
                                Text(
                                    text = "${artist.followers} Followers",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (!artist.genres.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(Spacing.S))
                                Text(
                                    text = "Genres: ${artist.genres}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(Spacing.L))
                        }
                    }

                    // Albums section if available
                    if (data.albums.isNotEmpty()) {
                        item {
                            Text(
                                text = "Albums & Singles",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = Spacing.XL, vertical = Spacing.S)
                            )
                        }
                        items(data.albums) { album ->
                            AlbumListItem(album = album, onClick = { onAlbumClick(album.id) })
                        }
                    }

                    // Top Songs
                    if (data.topSongs.isNotEmpty()) {
                        item {
                            Text(
                                text = "Popular Tracks",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = Spacing.XL, vertical = Spacing.S)
                            )
                        }

                        items(data.topSongs) { song ->
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
}
