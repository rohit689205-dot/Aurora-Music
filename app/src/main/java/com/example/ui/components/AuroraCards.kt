package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.ui.theme.AuroraShapes
import com.example.ui.theme.Spacing

@Composable
fun MusicCard(
    song: Song,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(140.dp)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                .data(song.artworkUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Cover for ${song.title}",
            modifier = Modifier
                .size(140.dp)
                .clip(AuroraShapes.AlbumArtwork),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(Spacing.S))
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = song.artist,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ArtistCard(
    name: String,
    imageUrl: String,
    isVerified: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(140.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Photo of $name",
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(Spacing.S))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (isVerified) {
                Spacer(modifier = Modifier.width(Spacing.XS))
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Verified",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun PlaylistCard(
    title: String,
    creator: String,
    imageUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(160.dp)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Cover for $title",
            modifier = Modifier
                .size(160.dp)
                .clip(AuroraShapes.Card),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(Spacing.S))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "by $creator",
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = AuroraShapes.Card,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = Spacing.L)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(Spacing.M))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun SongListItem(
    song: Song,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = Spacing.XL, vertical = Spacing.S),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                .data(song.artworkUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Cover for ${song.title}",
            modifier = Modifier
                .size(56.dp)
                .clip(AuroraShapes.AlbumArtwork),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(Spacing.L))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
