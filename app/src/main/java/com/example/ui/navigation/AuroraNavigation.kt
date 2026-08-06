package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Home", Icons.Rounded.Home)
    object Search : Screen("search", "Search", Icons.Rounded.Search)
    object Library : Screen("library", "Library", Icons.Rounded.LibraryMusic)
    object Settings : Screen("settings", "Settings", Icons.Rounded.Settings)
}

@Composable
fun AuroraApp(playerViewModel: PlayerViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute !in listOf("splash", "welcome")

    val songsState by playerViewModel.allSongsState.collectAsStateWithLifecycle()
    val currentSong by playerViewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by playerViewModel.isPlaying.collectAsStateWithLifecycle()
    val progress by playerViewModel.progress.collectAsStateWithLifecycle()

    var isPlayerExpanded by remember { mutableStateOf(false) }

    val items = listOf(Screen.Home, Screen.Search, Screen.Library, Screen.Settings)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
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
                composable("playlist") {
                    com.example.ui.playlist.PlaylistScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("album") {
                    com.example.ui.album.AlbumScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("artist") {
                    com.example.ui.artist.ArtistScreen(
                        onBack = { navController.popBackStack() }
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
                        songsState = songsState,
                        onSongClick = { song -> playerViewModel.playSong(song) },
                        onNavigateToSearch = { navController.navigate(Screen.Search.route) }
                    )
                }
                composable(Screen.Search.route) {
                    com.example.ui.search.SearchScreen(
                        onBack = { navController.navigate(Screen.Home.route) }
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

            // Mini Player overlay
            if (showBottomBar) {
                currentSong?.let { song ->
                    if (!isPlayerExpanded) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = innerPadding.calculateBottomPadding())
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
}
