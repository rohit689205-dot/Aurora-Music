package com.example.ui.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val coroutineScope = rememberCoroutineScope()

    val pages = listOf(
        OnboardingPage("Welcome to Aurora", "Your music, beautifully experienced."),
        OnboardingPage("Discover Music", "Find new favorites curated just for you."),
        OnboardingPage("Offline Listening", "Take your tracks anywhere, no connection needed."),
        OnboardingPage("Smart Recommendations", "A continuous stream of music you'll love."),
        OnboardingPage("Get Started", "Let's dive into the world of Aurora Music.")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            OnboardingContent(pages[page])
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Page indicators
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(pages.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            if (pagerState.currentPage == pages.size - 1) {
                Button(onClick = onGetStarted) {
                    Text("Start")
                }
            } else {
                TextButton(onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }) {
                    Text("Next")
                }
            }
        }
    }
}

data class OnboardingPage(val title: String, val description: String)

@Composable
fun OnboardingContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text("Illustration", color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
