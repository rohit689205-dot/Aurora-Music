package com.example.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.model.Song
import com.example.ui.components.MusicCard
import com.example.ui.components.SkeletonMusicCard
import com.example.ui.components.SkeletonSongListItem
import com.example.ui.components.SongListItem
import com.example.ui.theme.Spacing

import com.example.ui.state.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    songsState: UiState<List<Song>>,
    onSongClick: (Song) -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp) // padding for mini player
    ) {
        item {
            Column(modifier = Modifier.padding(Spacing.XL)) {
                Text(
                    text = "Good Evening",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(Spacing.L))
                // Fake search bar
                Surface(
                    onClick = onNavigateToSearch,
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = Spacing.L)
                    ) {
                        Icon(Icons.Rounded.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(Spacing.M))
                        Text("Songs, artists, podcasts...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        
        item {
            Text(
                text = "Recently Played",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = Spacing.XL, vertical = Spacing.S)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = Spacing.XL),
                horizontalArrangement = Arrangement.spacedBy(Spacing.L)
            ) {
                when (songsState) {
                    is UiState.Loading -> items(3) { SkeletonMusicCard() }
                    is UiState.Success -> items(songsState.data.take(3)) { song ->
                        MusicCard(song = song, onClick = { onSongClick(song) })
                    }
                    else -> items(0) {} // Hide on empty or error
                }
            }
            Spacer(modifier = Modifier.height(Spacing.XL))
        }

        item {
            Text(
                text = "Recommended for you",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = Spacing.XL, vertical = Spacing.S)
            )
        }
        
        when (songsState) {
            is UiState.Loading -> items(5) { SkeletonSongListItem() }
            is UiState.Success -> items(songsState.data) { song ->
                SongListItem(song = song, onClick = { onSongClick(song) })
            }
            is UiState.Empty -> item {
                Box(modifier = Modifier.fillMaxWidth().padding(Spacing.XL), contentAlignment = Alignment.Center) {
                    Text("No recommendations found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            is UiState.Error -> item {
                Box(modifier = Modifier.fillMaxWidth().padding(Spacing.XL), contentAlignment = Alignment.Center) {
                    Text("Failed to load recommendations.", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
