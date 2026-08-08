package com.example.ui.lyrics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.api.model.LyricLine
import com.example.model.Song
import com.example.playback.AudioPlayerManager
import com.example.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LyricsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentSong by AudioPlayerManager.currentSong.collectAsStateWithLifecycle()
    val currentPositionMs by AudioPlayerManager.positionMs.collectAsStateWithLifecycle()
    val isPlaying by AudioPlayerManager.isPlaying.collectAsStateWithLifecycle()

    var showPlainLyrics by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentSong?.title ?: "Lyrics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        if (currentSong != null) {
                            Text(
                                text = currentSong!!.artist,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.retryLoad() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Lyrics")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is LyricsUiState.Idle, is LyricsUiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(Spacing.M))
                        Text(
                            text = "Fetching lyrics from LRCLIB & Echo Music API...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is LyricsUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Spacing.L),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(Spacing.M))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(Spacing.L))
                        Button(onClick = { viewModel.retryLoad() }) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(Spacing.XS))
                            Text("Retry Lyrics Search")
                        }
                    }
                }

                is LyricsUiState.Success -> {
                    LyricsContent(
                        song = state.song,
                        lyricLines = state.lyricLines,
                        plainLyrics = state.plainLyrics,
                        provider = state.provider,
                        isSynced = state.isSynced,
                        currentPositionMs = currentPositionMs,
                        showPlainLyrics = showPlainLyrics,
                        onTogglePlain = { showPlainLyrics = !showPlainLyrics },
                        onLineClick = { timestampMs ->
                            viewModel.seekToTimestamp(timestampMs)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LyricsContent(
    song: Song,
    lyricLines: List<LyricLine>,
    plainLyrics: String?,
    provider: String,
    isSynced: Boolean,
    currentPositionMs: Long,
    showPlainLyrics: Boolean,
    onTogglePlain: () -> Unit,
    onLineClick: (Long) -> Unit
) {
    // Determine active line index
    val activeIndex = remember(currentPositionMs, lyricLines) {
        if (!isSynced || lyricLines.isEmpty()) -1
        else {
            val idx = lyricLines.indexOfLast { it.timestampMs <= currentPositionMs }
            if (idx == -1) 0 else idx
        }
    }

    val listState = rememberLazyListState()

    // Smooth auto-scroll to active line
    LaunchedEffect(activeIndex) {
        if (activeIndex in lyricLines.indices && isSynced && !showPlainLyrics) {
            val targetIndex = (activeIndex - 2).coerceAtLeast(0)
            listState.animateScrollToItem(targetIndex)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Track summary header card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.M, vertical = Spacing.S),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.M),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = song.artworkUrl,
                    contentDescription = song.title,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(Spacing.S)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(Spacing.M))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Text(
                        text = provider,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (!plainLyrics.isNullOrBlank() && isSynced) {
                    FilterChip(
                        selected = showPlainLyrics,
                        onClick = onTogglePlain,
                        label = { Text(if (showPlainLyrics) "Plain" else "Synced") },
                        leadingIcon = {
                            Icon(Icons.Default.Subtitles, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }
        }

        // Lyrics display
        if (showPlainLyrics || !isSynced) {
            val textToDisplay = plainLyrics ?: lyricLines.joinToString("\n") { it.text }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacing.L, vertical = Spacing.M)
            ) {
                item {
                    Text(
                        text = textToDisplay,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 32.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(Spacing.XXL))
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacing.L),
                contentPadding = PaddingValues(vertical = Spacing.XL)
            ) {
                itemsIndexed(lyricLines) { index, line ->
                    val isActive = index == activeIndex

                    val textColor by animateColorAsState(
                        targetValue = if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        animationSpec = tween(durationMillis = 300),
                        label = "textColor"
                    )

                    val textScale by animateFloatAsState(
                        targetValue = if (isActive) 1.08f else 0.98f,
                        animationSpec = tween(durationMillis = 300),
                        label = "textScale"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.S)
                            .clip(RoundedCornerShape(Spacing.S))
                            .background(
                                if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                                else Color.Transparent
                            )
                            .clickable { onLineClick(line.timestampMs) }
                            .padding(horizontal = Spacing.M, vertical = Spacing.S)
                    ) {
                        Text(
                            text = line.text,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = textColor,
                            fontSize = if (isActive) 22.sp else 18.sp,
                            lineHeight = 30.sp,
                            modifier = Modifier.scale(textScale)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }
    }
}
