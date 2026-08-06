package com.example.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    
    val recentSearches = listOf("Synthwave Mix", "Lofi Beats", "Aurora Original Soundtrack")
    val trendingSearches = listOf("Top 50 Global", "Weekend Vibes", "New Releases")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search songs, artists, albums") },
                        modifier = Modifier.fillMaxWidth().padding(end = Spacing.M),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
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
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            if (query.isEmpty()) {
                item {
                    Text(
                        text = "Recent Searches",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = Spacing.XL, vertical = Spacing.M)
                    )
                }
                items(recentSearches) { search ->
                    SearchSuggestionItem(
                        icon = Icons.Rounded.History,
                        text = search,
                        onClick = { query = search }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(Spacing.L))
                    Text(
                        text = "Trending",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = Spacing.XL, vertical = Spacing.M)
                    )
                }
                items(trendingSearches) { search ->
                    SearchSuggestionItem(
                        icon = Icons.Rounded.TrendingUp,
                        text = search,
                        onClick = { query = search },
                        iconTint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.XXL),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Searching for \"$query\"...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchSuggestionItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.XL, vertical = Spacing.M),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(Spacing.L))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
