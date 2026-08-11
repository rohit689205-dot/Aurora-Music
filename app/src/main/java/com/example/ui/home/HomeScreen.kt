package com.example.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.model.Song
import com.example.ui.components.AuroraMusicCard
import com.example.ui.components.SkeletonMusicCard
import com.example.ui.search.SongListItem
import com.example.ui.state.UiState
import com.example.ui.theme.LocalAppDensity
import com.example.ui.theme.Spacing

@Composable
fun HomeScreen(
    onSongClick: (Song) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToHistory: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: HomeViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val homeState by viewModel.homeState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val density = LocalAppDensity.current

    val speechLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)?.getOrNull(0)
            if (!spokenText.isNullOrEmpty()) {
                android.widget.Toast.makeText(context, "Searching for: $spokenText", android.widget.Toast.LENGTH_SHORT).show()
                onNavigateToSearch()
            }
        }
    }

    var selectedSongForOptions by remember { mutableStateOf<Song?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // 1. GREETING / HEADER
        item {
            Column(modifier = Modifier.padding(horizontal = Spacing.XL, vertical = Spacing.M)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Namaste, Music Lover",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Aurora Music",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeaderIconButton(
                            icon = Icons.Rounded.Refresh,
                            contentDescription = "Refresh Home",
                            onClick = { viewModel.loadHomeData() }
                        )
                        HeaderIconButton(
                            icon = Icons.Outlined.History,
                            contentDescription = "Listening History",
                            onClick = onNavigateToHistory
                        )
                        HeaderIconButton(
                            icon = Icons.Outlined.Person,
                            contentDescription = "Profile",
                            onClick = onNavigateToProfile
                        )
                        HeaderIconButton(
                            icon = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            onClick = onNavigateToSettings
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.M))

                // Search launcher bar
                Surface(
                    onClick = onNavigateToSearch,
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("home_search_bar")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = Spacing.L)
                    ) {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(Spacing.M))
                        Text(
                            text = "Search Indian songs, artists, albums...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Speak to search Indian songs...")
                                }
                                try {
                                    speechLauncher.launch(intent)
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Voice search not supported on this device", android.widget.Toast.LENGTH_SHORT).show()
                                    onNavigateToSearch()
                                }
                            },
                            modifier = Modifier.testTag("home_mic_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Mic,
                                contentDescription = "Voice Search",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // MOOD CHIPS
        item {
            val selectedMood = (homeState as? HomeUiState.Success)?.data?.selectedMood
            LazyRow(
                contentPadding = PaddingValues(horizontal = Spacing.XL, vertical = Spacing.S),
                horizontalArrangement = Arrangement.spacedBy(Spacing.M)
            ) {
                items(viewModel.moodChips) { mood ->
                    val isSelected = selectedMood == mood
                    val chipBg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    val chipText = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

                    Surface(
                        onClick = { viewModel.selectMood(mood) },
                        shape = RoundedCornerShape(20.dp),
                        color = chipBg,
                        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)) else null,
                        modifier = Modifier
                            .height(density.chipHeight)
                            .testTag("mood_chip_${mood.lowercase()}")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = Spacing.L)
                        ) {
                            Text(
                                text = mood,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = chipText
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(Spacing.L))
        }

        when (val state = homeState) {
            is HomeUiState.Loading -> {
                item {
                    Column(modifier = Modifier.padding(horizontal = Spacing.XL)) {
                        Text(text = "Loading music...", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(Spacing.M))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.L)) {
                            items(4) { SkeletonMusicCard() }
                        }
                    }
                }
            }
            is HomeUiState.Error -> {
                item {
                    MusicErrorCard(
                        title = state.message,
                        message = "Please check your connection and try again.",
                        buttonText = "Retry",
                        onClick = { viewModel.retry() }
                    )
                }
            }
            is HomeUiState.Empty -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(Spacing.XXL), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No music found.", style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(Spacing.M))
                            Button(onClick = { viewModel.retry() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
            is HomeUiState.Success -> {
                val data = state.data

                // 2. INDIAN TRENDING
                item {
                    HomeSectionHeader(subtitle = "TRENDING NOW", title = "Indian Trending")
                    Spacer(modifier = Modifier.height(Spacing.M))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = Spacing.XL),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.L)
                    ) {
                        items(data.indianTrending) { song ->
                            AuroraMusicCard(
                                title = song.title,
                                artist = song.artist,
                                imageUrl = song.artworkUrl,
                                badgeText = "TRENDING",
                                onClick = { onSongClick(song) },
                                onMoreClick = { selectedSongForOptions = song }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(Spacing.XXL))
                }

                // 3. POPULAR IN INDIA
                item {
                    HomeSectionHeader(subtitle = "TOP CHARTS", title = "Popular in India")
                    Spacer(modifier = Modifier.height(Spacing.M))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = Spacing.XL),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.L)
                    ) {
                        items(data.popularInIndia) { song ->
                            AuroraMusicCard(
                                title = song.title,
                                artist = song.artist,
                                imageUrl = song.artworkUrl,
                                badgeText = "POPULAR",
                                onClick = { onSongClick(song) },
                                onMoreClick = { selectedSongForOptions = song }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(Spacing.XXL))
                }

                // 4. HINDI HITS
                item {
                    HomeSectionHeader(subtitle = "CURATED HITS", title = "Hindi Hits")
                    Spacer(modifier = Modifier.height(Spacing.M))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = Spacing.XL),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.L)
                    ) {
                        items(data.hindiHits) { song ->
                            AuroraMusicCard(
                                title = song.title,
                                artist = song.artist,
                                imageUrl = song.artworkUrl,
                                badgeText = "HINDI",
                                onClick = { onSongClick(song) },
                                onMoreClick = { selectedSongForOptions = song }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(Spacing.XXL))
                }

                // 5. PUNJABI HITS
                item {
                    HomeSectionHeader(subtitle = "CLUB ANTHEMS", title = "Punjabi Hits")
                    Spacer(modifier = Modifier.height(Spacing.M))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = Spacing.XL),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.L)
                    ) {
                        items(data.punjabiHits) { song ->
                            AuroraMusicCard(
                                title = song.title,
                                artist = song.artist,
                                imageUrl = song.artworkUrl,
                                badgeText = "PUNJABI",
                                onClick = { onSongClick(song) },
                                onMoreClick = { selectedSongForOptions = song }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(Spacing.XXL))
                }

                // 6. BOLLYWOOD / INDIAN FILM MUSIC
                item {
                    HomeSectionHeader(subtitle = "SOUNDTRACKS & FILMS", title = "Bollywood / Indian Film Music")
                    Spacer(modifier = Modifier.height(Spacing.M))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = Spacing.XL),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.L)
                    ) {
                        items(data.bollywoodMusic) { song ->
                            AuroraMusicCard(
                                title = song.title,
                                artist = song.artist,
                                imageUrl = song.artworkUrl,
                                badgeText = "BOLLYWOOD",
                                onClick = { onSongClick(song) },
                                onMoreClick = { selectedSongForOptions = song }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(Spacing.XXL))
                }

                // 7. INDIAN ARTISTS
                item {
                    HomeSectionHeader(subtitle = "TOP CREATORS", title = "Indian Artists")
                    Spacer(modifier = Modifier.height(Spacing.M))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = Spacing.XL),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.L)
                    ) {
                        items(data.indianArtists) { artist ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(110.dp)
                                    .clickable {}
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    AsyncImage(
                                        model = artist.image.ifEmpty { "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=300" },
                                        contentDescription = artist.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = artist.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(Spacing.XXL))
                }

                // 8. INDIAN ALBUMS
                item {
                    HomeSectionHeader(subtitle = "FEATURED RELEASES", title = "Indian Albums")
                    Spacer(modifier = Modifier.height(Spacing.M))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = Spacing.XL),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.L)
                    ) {
                        items(data.indianAlbums) { album ->
                            Column(
                                modifier = Modifier
                                    .width(140.dp)
                                    .clickable {}
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(140.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                ) {
                                    AsyncImage(
                                        model = album.artwork.ifEmpty { "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=300" },
                                        contentDescription = album.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = album.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(Spacing.XXL))
                }

                // 9. RECENTLY PLAYED
                item {
                    HomeSectionHeader(subtitle = "HISTORY", title = "Recently Played")
                    Spacer(modifier = Modifier.height(Spacing.M))
                }
                items(data.recentlyPlayed) { song ->
                    Box(modifier = Modifier.padding(horizontal = Spacing.XL)) {
                        SongListItem(
                            song = song,
                            onClick = { onSongClick(song) },
                            onInfoClick = { selectedSongForOptions = song }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(Spacing.XXL)) }

                // 10. YOUR PLAYLISTS
                item {
                    HomeSectionHeader(subtitle = "CURATED FOR YOU", title = "Your Playlists")
                    Spacer(modifier = Modifier.height(Spacing.M))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = Spacing.XL),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.L)
                    ) {
                        items(data.userPlaylists) { playlist ->
                            Card(
                                onClick = {},
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier
                                    .width(180.dp)
                                    .height(140.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(Spacing.M),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.MusicNote,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Column {
                                        Text(
                                            text = playlist.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = playlist.description ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(Spacing.XXL))
                }
            }
        }
    }

    // SONG OPTIONS BOTTOM SHEET
    selectedSongForOptions?.let { song ->
        SongOptionsBottomSheet(
            song = song,
            onDismiss = { selectedSongForOptions = null },
            onPlay = {
                onSongClick(song)
                selectedSongForOptions = null
            }
        )
    }
}

@Composable
private fun HomeSectionHeader(subtitle: String, title: String) {
    Column(modifier = Modifier.padding(horizontal = Spacing.XL)) {
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun HeaderIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "headerIconScale"
    )

    IconButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(24.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongOptionsBottomSheet(
    song: Song,
    onDismiss: () -> Unit,
    onPlay: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.XL)
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = song.artist.ifEmpty { "Artist" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.L))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(Spacing.M))

            ListItem(
                headlineContent = { Text("Play Song") },
                leadingContent = { Icon(Icons.Rounded.MusicNote, contentDescription = null) },
                modifier = Modifier.clickable { onPlay() }
            )
            ListItem(
                headlineContent = { Text("Add to Favorites") },
                leadingContent = { Icon(Icons.Outlined.History, contentDescription = null) },
                modifier = Modifier.clickable { onDismiss() }
            )
            ListItem(
                headlineContent = { Text("View Artist") },
                leadingContent = { Icon(Icons.Outlined.Person, contentDescription = null) },
                modifier = Modifier.clickable { onDismiss() }
            )

            Spacer(modifier = Modifier.height(Spacing.L))
        }
    }
}

@Composable
fun MusicErrorCard(
    title: String,
    message: String,
    buttonText: String = "Retry",
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.XL, vertical = Spacing.XL),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.XL),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Rounded.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(Spacing.M))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Spacing.S))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Spacing.L))
            Button(
                onClick = onClick,
                modifier = Modifier.testTag("music_error_button")
            ) {
                Text(buttonText)
            }
        }
    }
}

