package com.example.ui.home

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.Song
import com.example.ui.components.AuroraMusicCard
import com.example.ui.components.GenreGridCard
import com.example.ui.components.SkeletonMusicCard
import com.example.ui.components.SkeletonSongListItem
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
    val density = LocalAppDensity.current

    var selectedSongForOptions by remember { mutableStateOf<Song?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // TOP APP BAR
        item {
            Column(modifier = Modifier.padding(horizontal = Spacing.XL, vertical = Spacing.M)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Aurora Music",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeaderIconButton(
                            icon = Icons.Outlined.History,
                            contentDescription = "Listening History",
                            onClick = onNavigateToHistory
                        )
                        HeaderIconButton(
                            icon = Icons.Outlined.TrendingUp,
                            contentDescription = "Trending",
                            onClick = { viewModel.loadHomeData("Trending") }
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
                            text = "Search songs, artists, playlists...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // MOOD CHIPS
        item {
            val selectedMood = (homeState as? UiState.Success)?.data?.selectedMood
            LazyRow(
                contentPadding = PaddingValues(horizontal = Spacing.XL, vertical = Spacing.S),
                horizontalArrangement = Arrangement.spacedBy(Spacing.M)
            ) {
                items(viewModel.moodChips) { mood ->
                    val isSelected = selectedMood == mood
                    val chipBg = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                    val chipText = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }

                    Surface(
                        onClick = { viewModel.selectMood(mood) },
                        shape = RoundedCornerShape(20.dp),
                        color = chipBg,
                        border = if (!isSelected) {
                            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        } else null,
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

        // HERO SECTION: MUSIC THAT'S HOT AND HAPPENING!
        item {
            Column(modifier = Modifier.padding(horizontal = Spacing.XL)) {
                Text(
                    text = "MUSIC THAT'S HOT AND HAPPENING!",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Top Global Hits",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(Spacing.M))

            val biggestHits = (homeState as? UiState.Success)?.data?.biggestHitsCards ?: emptyList()
            LazyRow(
                contentPadding = PaddingValues(horizontal = Spacing.XL),
                horizontalArrangement = Arrangement.spacedBy(Spacing.L)
            ) {
                if (biggestHits.isEmpty()) {
                    items(4) { SkeletonMusicCard() }
                } else {
                    items(biggestHits) { cardItem ->
                        AuroraMusicCard(
                            title = cardItem.title,
                            artist = cardItem.artist,
                            imageUrl = cardItem.imageUrl,
                            badgeText = cardItem.badge,
                            onClick = { onSongClick(cardItem.song) },
                            onMoreClick = { selectedSongForOptions = cardItem.song }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.XXL))
        }

        // MOOD AND GENRES SECTION
        item {
            val selectedGenre = (homeState as? UiState.Success)?.data?.selectedGenre
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.XL),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mood and Genres",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(onClick = { onNavigateToSearch() }) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowForward,
                        contentDescription = "See All Genres",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.M))

            // 3-column grid for genres
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.XL),
                verticalArrangement = Arrangement.spacedBy(Spacing.M)
            ) {
                val genreChunks = viewModel.genres.chunked(3)
                genreChunks.forEach { rowGenres ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.M)
                    ) {
                        rowGenres.forEach { genre ->
                            GenreGridCard(
                                name = genre,
                                isSelected = selectedGenre == genre,
                                onClick = { viewModel.selectGenre(genre) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.XXL))
        }

        // DANCING HITS SECTION
        item {
            Column(modifier = Modifier.padding(horizontal = Spacing.XL)) {
                Text(
                    text = "DANCE YOUR STRESS AWAY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Dancing Hits",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(Spacing.M))

            val dancingHits = (homeState as? UiState.Success)?.data?.dancingHitsCards ?: emptyList()
            LazyRow(
                contentPadding = PaddingValues(horizontal = Spacing.XL),
                horizontalArrangement = Arrangement.spacedBy(Spacing.L)
            ) {
                if (dancingHits.isEmpty()) {
                    items(3) { SkeletonMusicCard() }
                } else {
                    items(dancingHits) { cardItem ->
                        AuroraMusicCard(
                            title = cardItem.title,
                            artist = cardItem.artist,
                            imageUrl = cardItem.imageUrl,
                            badgeText = cardItem.badge,
                            onClick = { onSongClick(cardItem.song) },
                            onMoreClick = { selectedSongForOptions = cardItem.song }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.XXL))
        }

        // RECOMMENDED TRACKS LIST
        item {
            Text(
                text = "Recommended for You",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = Spacing.XL, vertical = Spacing.S)
            )
        }

        when (val state = homeState) {
            is UiState.Loading -> items(6) { SkeletonSongListItem() }
            is UiState.Success -> items(state.data.categorySongs) { song ->
                SongListItem(
                    song = song,
                    onClick = { onSongClick(song) },
                    onInfoClick = { selectedSongForOptions = song }
                )
            }
            is UiState.Error -> item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.XL, vertical = Spacing.L),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.L),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(Spacing.M))
                        Button(
                            onClick = { viewModel.retry() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Retry",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(Spacing.S))
                            Text("Retry")
                        }
                    }
                }
            }
            is UiState.Empty -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.XL),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No music found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
