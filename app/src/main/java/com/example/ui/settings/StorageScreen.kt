package com.example.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Storage Management", fontWeight = FontWeight.Bold) },
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
            // Storage Summary
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.XL),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "2.4 GB Used",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "10 GB Free",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(Spacing.L))
                
                LinearProgressIndicator(
                    progress = { 0.24f },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            
            HorizontalDivider()
            
            SettingsCategory("Maintenance")
            
            SettingsItem(
                icon = Icons.Rounded.Delete,
                title = "Clear Downloads",
                subtitle = "1.2 GB of downloaded music",
                onClick = { /* TODO */ }
            )
            
            SettingsItem(
                icon = Icons.Rounded.CleaningServices,
                title = "Clear Image Cache",
                subtitle = "140 MB of album artwork",
                onClick = { /* TODO */ }
            )
        }
    }
}
