package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AuroraShapes
import com.example.ui.theme.Spacing

@Composable
fun ShimmerAnimation(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            Color.LightGray.copy(alpha = 0.2f),
            Color.LightGray.copy(alpha = 0.5f),
            Color.LightGray.copy(alpha = 0.2f),
        ),
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    Box(modifier = modifier.background(brush))
}

@Composable
fun SkeletonSongListItem(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.XL, vertical = Spacing.S)
    ) {
        ShimmerAnimation(
            modifier = Modifier
                .size(56.dp)
                .clip(AuroraShapes.AlbumArtwork)
        )
        Spacer(modifier = Modifier.width(Spacing.L))
        Column(modifier = Modifier.weight(1f)) {
            ShimmerAnimation(
                modifier = Modifier
                    .height(20.dp)
                    .fillMaxWidth(0.7f)
                    .clip(AuroraShapes.Chip)
            )
            Spacer(modifier = Modifier.height(Spacing.S))
            ShimmerAnimation(
                modifier = Modifier
                    .height(16.dp)
                    .fillMaxWidth(0.4f)
                    .clip(AuroraShapes.Chip)
            )
        }
    }
}

@Composable
fun SkeletonMusicCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.width(140.dp)
    ) {
        ShimmerAnimation(
            modifier = Modifier
                .size(140.dp)
                .clip(AuroraShapes.AlbumArtwork)
        )
        Spacer(modifier = Modifier.height(Spacing.S))
        ShimmerAnimation(
            modifier = Modifier
                .height(20.dp)
                .fillMaxWidth(0.9f)
                .clip(AuroraShapes.Chip)
        )
        Spacer(modifier = Modifier.height(Spacing.S))
        ShimmerAnimation(
            modifier = Modifier
                .height(16.dp)
                .fillMaxWidth(0.6f)
                .clip(AuroraShapes.Chip)
        )
    }
}
