package com.example.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Song
import com.example.ui.components.SongListItem
import com.example.ui.components.SkeletonSongListItem
import com.example.ui.components.QuickActionCard
import com.example.ui.theme.Spacing

import com.example.ui.state.UiState
import androidx.compose.ui.Alignment

@Composable
fun LibraryScreen(
    songsState: UiState<List<Song>>,
    onSongClick: (Song) -> Unit,
    onNavigateToDownloads: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(Spacing.XL)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Library",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row {
                        IconButton(onClick = onNavigateToSearch) {
                            Icon(Icons.Rounded.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { /* TODO */ }) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(Spacing.L))
                
                // Quick Actions
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.S)) {
                    QuickActionCard(
                        icon = Icons.Rounded.PlayArrow,
                        label = "Play All",
                        onClick = { /* TODO */ },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        icon = Icons.Rounded.Shuffle,
                        label = "Shuffle All",
                        onClick = { /* TODO */ },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(Spacing.S))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.S)) {
                    QuickActionCard(
                        icon = Icons.Rounded.Favorite,
                        label = "Favorites",
                        onClick = { /* TODO */ },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        icon = Icons.Rounded.Download,
                        label = "Downloads",
                        onClick = onNavigateToDownloads,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(Spacing.S))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.S)) {
                    QuickActionCard(
                        icon = Icons.Rounded.Folder,
                        label = "Local Files",
                        onClick = { /* TODO */ },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        icon = Icons.Rounded.History,
                        label = "History",
                        onClick = onNavigateToHistory,
                        modifier = Modifier.weight(1f)
                    )
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
        }
        
        when (songsState) {
            is UiState.Loading -> items(10) { SkeletonSongListItem() }
            is UiState.Success -> items(songsState.data) { song ->
                SongListItem(song = song, onClick = { onSongClick(song) })
            }
            is UiState.Empty -> item {
                Box(modifier = Modifier.fillMaxWidth().padding(Spacing.XL), contentAlignment = Alignment.Center) {
                    Text("Your library is empty.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            is UiState.Error -> item {
                Box(modifier = Modifier.fillMaxWidth().padding(Spacing.XL), contentAlignment = Alignment.Center) {
                    Text("Failed to load library.", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
