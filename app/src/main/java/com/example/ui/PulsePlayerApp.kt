package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.EqualizerScreen
import com.example.ui.screens.QueueScreen
import com.example.ui.screens.LyricsScreen
import com.example.viewmodel.MusicViewModel

import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.basicMarquee
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

import com.example.ui.screens.SearchScreen

import com.example.ui.screens.StatsScreen

@Composable
fun PulsePlayerApp(viewModel: MusicViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    var isPlayerOpen by remember { mutableStateOf(false) }
    var isEqualizerOpen by remember { mutableStateOf(false) }
    var isQueueOpen by remember { mutableStateOf(false) }
    var isLyricsOpen by remember { mutableStateOf(false) }

    val currentTrack = viewModel.currentTrack.collectAsState().value
    val isPlaying = viewModel.isPlaying.collectAsState().value

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                Column {
                    // Mini Player
                    AnimatedVisibility(
                        visible = currentTrack != null && selectedTab != 7,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it })
                    ) {
                        currentTrack?.let { track ->
                            Column {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(64.dp)
                                        .clickable { viewModel.navigateToTab(7) }
                                        .pointerInput(Unit) {
                                            detectHorizontalDragGestures { _, dragAmount ->
                                                if (dragAmount > 50) {
                                                    viewModel.previousTrack()
                                                } else if (dragAmount < -50) {
                                                    viewModel.nextTrack()
                                                }
                                            }
                                        },
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    tonalElevation = 8.dp
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = track.albumArtUri,
                                            contentDescription = "Album Art",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.size(48.dp).background(Color.DarkGray)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = track.title,
                                                color = Color.White,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                modifier = Modifier.basicMarquee()
                                            )
                                            Text(
                                                text = track.artist,
                                                color = Color.White.copy(alpha = 0.7f),
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.previousTrack() }
                                        ) {
                                            Icon(
                                                Icons.Rounded.SkipPrevious,
                                                contentDescription = "Previous",
                                                tint = Color.White
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.togglePlayPause() }
                                        ) {
                                            Icon(
                                                if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                                contentDescription = "Play/Pause",
                                                tint = Color.White
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.nextTrack() }
                                        ) {
                                            Icon(
                                                Icons.Rounded.SkipNext,
                                                contentDescription = "Next",
                                                tint = Color.White
                                            )
                                        }
                                        IconButton(
                                            onClick = { isQueueOpen = true }
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.QueueMusic,
                                                contentDescription = "Queue",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        tonalElevation = 0.dp
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { viewModel.navigateToTab(0) },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                                selectedIconColor = Color(0xFF1D192B),
                                selectedTextColor = Color.White,
                                indicatorColor = MaterialTheme.colorScheme.secondary
                            )
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { viewModel.navigateToTab(1) },
                            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                            label = { Text("Search", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                                selectedIconColor = Color(0xFF1D192B),
                                selectedTextColor = Color.White,
                                indicatorColor = MaterialTheme.colorScheme.secondary
                            )
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { viewModel.navigateToTab(2) },
                            icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Library") },
                            label = { Text("Library", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                                selectedIconColor = Color(0xFF1D192B),
                                selectedTextColor = Color.White,
                                indicatorColor = MaterialTheme.colorScheme.secondary
                            )
                        )
                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { viewModel.navigateToTab(3) },
                            icon = { Icon(Icons.AutoMirrored.Filled.PlaylistPlay, contentDescription = "Playlists") },
                            label = { Text("Playlists", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                                selectedIconColor = Color(0xFF1D192B),
                                selectedTextColor = Color.White,
                                indicatorColor = MaterialTheme.colorScheme.secondary
                            )
                        )
                        NavigationBarItem(
                            selected = selectedTab == 7,
                            onClick = { viewModel.navigateToTab(7) },
                            icon = { Icon(Icons.Default.PlayCircle, contentDescription = "Now Playing") },
                            label = { Text("Playing", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                                selectedIconColor = Color(0xFF1D192B),
                                selectedTextColor = Color.White,
                                indicatorColor = MaterialTheme.colorScheme.secondary
                            )
                        )
                        NavigationBarItem(
                            selected = selectedTab == 5,
                            onClick = { viewModel.navigateToTab(5) },
                            icon = { Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = "Queue") },
                            label = { Text("Queue", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                                selectedIconColor = Color(0xFF1D192B),
                                selectedTextColor = Color.White,
                                indicatorColor = MaterialTheme.colorScheme.secondary
                            )
                        )
                        NavigationBarItem(
                            selected = selectedTab == 4,
                            onClick = { viewModel.navigateToTab(4) },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text("Settings", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                                selectedIconColor = Color(0xFF1D192B),
                                selectedTextColor = Color.White,
                                indicatorColor = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut()
                            )
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut()
                            )
                        }
                    },
                    label = "PageTransition"
                ) { targetTab ->
                    when (targetTab) {
                        0 -> HomeScreen(
                            viewModel = viewModel, 
                            onNavigateToPlayer = { viewModel.navigateToTab(7) }, 
                            onOpenEqualizer = { isEqualizerOpen = true },
                            onOpenStats = { viewModel.navigateToTab(6) },
                            onOpenQueue = { viewModel.navigateToTab(5) }
                        )
                        1 -> SearchScreen(viewModel = viewModel)
                        2 -> LibraryScreen(viewModel = viewModel)
                        3 -> PlaylistsScreen(viewModel = viewModel)
                        4 -> SettingsScreen(viewModel = viewModel)
                        5 -> QueueScreen(viewModel = viewModel, onBack = { viewModel.navigateToTab(0) })
                        6 -> StatsScreen(viewModel = viewModel)
                        7 -> PlayerScreen(
                            viewModel = viewModel,
                            onClose = { viewModel.navigateToTab(0) },
                            onOpenLyrics = { isLyricsOpen = true },
                            onOpenQueue = { viewModel.navigateToTab(5) },
                            onOpenEqualizer = { isEqualizerOpen = true }
                        )
                    }
                }
            }
        }
        
        if (isLyricsOpen) {
            Dialog(
                onDismissRequest = { isLyricsOpen = false },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnBackPress = true
                )
            ) {
                LyricsScreen(viewModel = viewModel, onBack = { isLyricsOpen = false })
            }
        }
        
        if (isEqualizerOpen) {
            Dialog(
                onDismissRequest = { isEqualizerOpen = false },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnBackPress = true
                )
            ) {
                EqualizerScreen(viewModel = viewModel, onBack = { isEqualizerOpen = false })
            }
        }
        
        if (isQueueOpen) {
            Dialog(
                onDismissRequest = { isQueueOpen = false },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnBackPress = true
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    QueueScreen(viewModel = viewModel, onBack = { isQueueOpen = false })
                }
            }
        }
    }
}
