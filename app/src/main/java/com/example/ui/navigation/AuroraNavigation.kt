package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.home.HomeScreen
import com.example.ui.library.LibraryScreen
import com.example.ui.player.ExpandedPlayer
import com.example.ui.player.MiniPlayer
import com.example.ui.player.PlayerViewModel
import com.example.ui.theme.Spacing

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Home", Icons.Rounded.Home)
    object Search : Screen("search", "Search", Icons.Rounded.Search)
    object Library : Screen("library", "Library", Icons.Rounded.LibraryMusic)
    object Settings : Screen("settings", "Settings", Icons.Rounded.Settings)
}

enum class FloatingNavItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String?
) {
    HOME("Home", Icons.Rounded.Home, Screen.Home.route),
    SEARCH("Search", Icons.Rounded.Search, Screen.Search.route),
    VOICE("Voice Search", Icons.Rounded.Mic, null),
    LIBRARY("Library", Icons.Rounded.LibraryMusic, Screen.Library.route),
    MORE("More", Icons.Rounded.MoreHoriz, null)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuroraApp(
    playerViewModel: PlayerViewModel = viewModel(),
    updateViewModel: com.example.update.UpdateViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute !in listOf("splash", "welcome")

    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                (context as? android.app.Activity)?.let { activity ->
                    updateViewModel.checkResumeUpdate(activity)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val songsState by playerViewModel.allSongsState.collectAsStateWithLifecycle()
    val currentSong by playerViewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by playerViewModel.isPlaying.collectAsStateWithLifecycle()
    val progress by playerViewModel.progress.collectAsStateWithLifecycle()

    var isPlayerExpanded by remember { mutableStateOf(false) }
    var showMoreMenuSheet by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                FloatingPillBottomBar(
                    currentRoute = currentRoute,
                    onItemClick = { navItem ->
                        when (navItem) {
                            FloatingNavItem.HOME,
                            FloatingNavItem.SEARCH,
                            FloatingNavItem.LIBRARY -> {
                                navItem.route?.let { route ->
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                            FloatingNavItem.VOICE -> {
                                navController.navigate(Screen.Search.route)
                            }
                            FloatingNavItem.MORE -> {
                                showMoreMenuSheet = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = "splash",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("splash") {
                    com.example.ui.splash.SplashScreen(
                        onTimeout = { navController.navigate("welcome") { popUpTo("splash") { inclusive = true } } }
                    )
                }
                composable("welcome") {
                    com.example.ui.welcome.WelcomeScreen(
                        onGetStarted = { navController.navigate(Screen.Home.route) { popUpTo("welcome") { inclusive = true } } }
                    )
                }
                composable("playlist/{playlistId}") { backStackEntry ->
                    val playlistId = backStackEntry.arguments?.getString("playlistId") ?: "PLDISKgbnvpk7sB1kRjLpA4gT_"
                    com.example.ui.playlist.PlaylistScreen(
                        playlistId = playlistId,
                        onBack = { navController.popBackStack() },
                        onSongClick = { song -> playerViewModel.playSong(song) }
                    )
                }
                composable("playlist") {
                    com.example.ui.playlist.PlaylistScreen(
                        onBack = { navController.popBackStack() },
                        onSongClick = { song -> playerViewModel.playSong(song) }
                    )
                }
                composable("album") {
                    com.example.ui.album.AlbumScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("artist/{channelId}") { backStackEntry ->
                    val channelId = backStackEntry.arguments?.getString("channelId") ?: "M83"
                    com.example.ui.artist.ArtistScreen(
                        channelIdOrName = channelId,
                        onBack = { navController.popBackStack() },
                        onSongClick = { song -> playerViewModel.playSong(song) }
                    )
                }
                composable("artist") {
                    com.example.ui.artist.ArtistScreen(
                        onBack = { navController.popBackStack() },
                        onSongClick = { song -> playerViewModel.playSong(song) }
                    )
                }
                composable("podcast") {
                    com.example.ui.podcast.PodcastScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("lyrics") {
                    com.example.ui.lyrics.LyricsScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("profile") {
                    com.example.ui.profile.ProfileScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.Home.route) {
                    HomeScreen(
                        onSongClick = { song -> playerViewModel.playSong(song) },
                        onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                        onNavigateToHistory = { navController.navigate("history") },
                        onNavigateToProfile = { navController.navigate("profile") },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                    )
                }
                composable(Screen.Search.route) {
                    com.example.ui.search.SearchScreen(
                        onBack = { navController.navigate(Screen.Home.route) },
                        onSongSelect = { song -> playerViewModel.playSong(song) },
                        onPlaylistSelect = { playlistId -> navController.navigate("playlist/$playlistId") },
                        onArtistSelect = { channelId -> navController.navigate("artist/$channelId") }
                    )
                }
                composable(Screen.Library.route) {
                    LibraryScreen(
                        songsState = songsState,
                        onSongClick = { song -> playerViewModel.playSong(song) },
                        onNavigateToDownloads = { navController.navigate("downloads") },
                        onNavigateToHistory = { navController.navigate("history") },
                        onNavigateToSearch = { navController.navigate(Screen.Search.route) }
                    )
                }
                composable(Screen.Settings.route) {
                    com.example.ui.settings.SettingsScreen(
                        onNavigateToStorage = { navController.navigate("storage") }
                    )
                }
                composable("queue") {
                    val queueState = songsState
                    val queueList = if (queueState is com.example.ui.state.UiState.Success) queueState.data else emptyList()
                    com.example.ui.player.QueueScreen(
                        currentSong = currentSong,
                        queue = queueList,
                        onBack = { navController.popBackStack() },
                        onSongClick = { song -> playerViewModel.playSong(song) }
                    )
                }
                composable("downloads") {
                    com.example.ui.library.DownloadsScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("history") {
                    com.example.ui.library.HistoryScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("storage") {
                    com.example.ui.settings.StorageScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            // In-App Update Overlay (Progress / Restart Banner / Flexible Dialog)
            com.example.update.AuroraUpdateOverlay(
                updateViewModel = updateViewModel,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // Mini Player overlay floating above bottom bar
            if (showBottomBar) {
                currentSong?.let { song ->
                    if (!isPlayerExpanded) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 76.dp)
                        ) {
                            MiniPlayer(
                                song = song,
                                isPlaying = isPlaying,
                                progress = progress,
                                onPlayPause = { playerViewModel.togglePlayPause() },
                                onNext = { playerViewModel.nextSong() },
                                onPrevious = { playerViewModel.previousSong() },
                                onClick = { isPlayerExpanded = true }
                            )
                        }
                    }
                }
            }
        }
    }

    // Expanded Player Full Screen Overlay
    androidx.activity.compose.BackHandler(enabled = isPlayerExpanded) {
        isPlayerExpanded = false
    }

    AnimatedVisibility(
        visible = isPlayerExpanded,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        currentSong?.let { song ->
            ExpandedPlayer(
                song = song,
                isPlaying = isPlaying,
                progress = progress,
                onPlayPause = { playerViewModel.togglePlayPause() },
                onNext = { playerViewModel.nextSong() },
                onPrevious = { playerViewModel.previousSong() },
                onClose = { isPlayerExpanded = false },
                onQueueClick = { 
                    isPlayerExpanded = false
                    navController.navigate("queue")
                }
            )
        }
    }

    // MORE MENU BOTTOM SHEET
    if (showMoreMenuSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreMenuSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.XL)
            ) {
                Text(
                    text = "Aurora Navigation",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Spacing.M))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(Spacing.M))

                MoreMenuItem("Queue", Icons.Outlined.QueueMusic) {
                    showMoreMenuSheet = false
                    navController.navigate("queue")
                }
                MoreMenuItem("Downloads", Icons.Outlined.Download) {
                    showMoreMenuSheet = false
                    navController.navigate("downloads")
                }
                MoreMenuItem("Playlists", Icons.Outlined.PlaylistPlay) {
                    showMoreMenuSheet = false
                    navController.navigate("playlist")
                }
                MoreMenuItem("Recently Played", Icons.Outlined.History) {
                    showMoreMenuSheet = false
                    navController.navigate("history")
                }
                MoreMenuItem("Favorites", Icons.Outlined.FavoriteBorder) {
                    showMoreMenuSheet = false
                    navController.navigate(Screen.Library.route)
                }
                MoreMenuItem("Settings", Icons.Outlined.Settings) {
                    showMoreMenuSheet = false
                    navController.navigate(Screen.Settings.route)
                }
                MoreMenuItem("About Aurora Music", Icons.Outlined.Info) {
                    showMoreMenuSheet = false
                    showAboutDialog = true
                }

                Spacer(modifier = Modifier.height(Spacing.L))
            }
        }
    }

    // ABOUT DIALOG
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Aurora Music", fontWeight = FontWeight.Bold) },
            text = {
                Text("Version 2.0 (Aurora Edition)\n\nA modern, clean, music-focused streaming experience with Light and Dark themes, customizable density, and rich audio controls.")
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun MoreMenuItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(label, fontWeight = FontWeight.SemiBold) },
        leadingContent = { Icon(icon, contentDescription = label) },
        modifier = Modifier
            .clickable { onClick() }
            .testTag("more_item_${label.lowercase().replace(" ", "_")}")
    )
}

@Composable
fun FloatingPillBottomBar(
    currentRoute: String?,
    onItemClick: (FloatingNavItem) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.XL, vertical = Spacing.M)
            .windowInsetsPadding(WindowInsets.navigationBars),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            shadowElevation = 8.dp,
            modifier = Modifier.height(64.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = Spacing.M)
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingNavItem.values().forEach { item ->
                    val isSelected = currentRoute == item.route

                    val containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Transparent
                    }

                    val iconTint = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(containerColor)
                            .clickable { onItemClick(item) }
                            .testTag("floating_nav_${item.name.lowercase()}")
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

