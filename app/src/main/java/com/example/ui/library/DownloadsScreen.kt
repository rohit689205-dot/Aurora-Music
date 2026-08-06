package com.example.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.components.EmptyScreen
import com.example.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Songs", "Albums", "Playlists", "Podcasts")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Downloads", fontWeight = FontWeight.Bold) },
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
        Column(modifier = modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                edgePadding = Spacing.L
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            // Display Empty state for now since we don't have a real download manager
            EmptyScreen(
                icon = Icons.Rounded.CloudOff,
                title = "No downloads yet",
                message = "Music you download will appear here for offline listening."
            )
        }
    }
}
