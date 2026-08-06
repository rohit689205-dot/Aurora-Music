package com.example.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.model.Song

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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.systemBars.asPaddingValues())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
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
                IconButton(onClick = { /* TODO: Player Menu */ }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Rounded.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            AsyncImage(
                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
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

            Spacer(modifier = Modifier.height(48.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.headlineLarge,
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
                IconButton(onClick = { /* TODO: Favorite */ }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(onClick = { /* TODO: Download */ }) {
                    Icon(imageVector = androidx.compose.material.icons.Icons.Rounded.Download, contentDescription = "Download", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { /* TODO: Add to Playlist */ }) {
                    Icon(imageVector = Icons.AutoMirrored.Rounded.PlaylistAdd, contentDescription = "Add to playlist", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { /* TODO: Share */ }) {
                    Icon(imageVector = androidx.compose.material.icons.Icons.Rounded.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Slider(
                value = progress,
                onValueChange = {},
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val currentMs = (song.duration * progress).toLong()
                val currentMinutes = (currentMs / 1000) / 60
                val currentSeconds = (currentMs / 1000) % 60
                Text("${currentMinutes}:${currentSeconds.toString().padStart(2, '0')}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                // format duration
                val minutes = (song.duration / 1000) / 60
                val seconds = (song.duration / 1000) % 60
                Text("${minutes}:${seconds.toString().padStart(2, '0')}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* TODO */ }) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onPrevious, modifier = Modifier.size(64.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(48.dp)
                    )
                }

                IconButton(onClick = onPlayPause, modifier = Modifier.size(96.dp)) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.PauseCircleFilled else Icons.Rounded.PlayCircleFilled,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(96.dp)
                    )
                }

                IconButton(onClick = onNext, modifier = Modifier.size(64.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(48.dp)
                    )
                }

                IconButton(onClick = { /* TODO */ }) {
                    Icon(
                        imageVector = Icons.Rounded.Repeat,
                        contentDescription = "Repeat",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* TODO: Sound Quality/Equalizer */ }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Rounded.Speaker,
                        contentDescription = "Audio options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onQueueClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                        contentDescription = "Queue",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
