package com.example.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        delay(2000L)
        onTimeout()
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Aurora Music", style = MaterialTheme.typography.displayLarge)
            Spacer(modifier = Modifier.height(32.dp))
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Version 1.0", style = MaterialTheme.typography.labelSmall)
        }
    }
}
