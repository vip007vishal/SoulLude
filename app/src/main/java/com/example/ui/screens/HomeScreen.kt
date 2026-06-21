package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AudioTrack
import com.example.ui.theme.GradientBottom
import com.example.ui.theme.GradientTop
import com.example.ui.theme.getThemeGradientColors
import com.example.viewmodel.MusicViewModel

@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    onNavigateToPlayer: () -> Unit,
    onOpenEqualizer: () -> Unit = {},
    onOpenStats: () -> Unit = {},
    onOpenQueue: () -> Unit = {}
) {
    val tracks = viewModel.tracks.collectAsState().value
    
    // Simulate mocked sections based on real local data (shuffled/recent)
    val recentlyPlayed = tracks.take(10).reversed()
    val recommended = tracks.shuffled().take(5)

    val themeMode by viewModel.themeMode.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = getThemeGradientColors(themeMode)
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp, top = 16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                        val greeting = if (currentHour in 5..11) "Good Morning" else if (currentHour in 12..17) "Good Afternoon" else "Good Evening"
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 28.sp
                            )
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.05f))
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White)
                        }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.05f))
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                        }
                    }
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha=0.05f)).padding(16.dp)) {
                        Column {
                            Icon(Icons.Default.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("142", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                            Text("Songs played today", color = Color.White.copy(alpha=0.6f), fontSize = 12.sp)
                            Spacer(Modifier.height(4.dp))
                            Text("Total songs in library: ${tracks.size}", color = Color.White.copy(alpha=0.4f), fontSize = 10.sp)
                        }
                    }
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha=0.05f)).padding(16.dp)) {
                        Column {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("5h 12m", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                            Text("Listening time today", color = Color.White.copy(alpha=0.6f), fontSize = 12.sp)
                            Spacer(Modifier.height(4.dp))
                            Text("Favorite: The Beatles", color = Color.White.copy(alpha=0.4f), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable { }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Search songs, artists, albums, folders...", color = Color.White.copy(alpha = 0.6f), modifier = Modifier.weight(1f))
                        Icon(Icons.Default.Mic, contentDescription = "Voice Search", tint = Color.White.copy(alpha = 0.6f))
                    }
                }
            }

            item {
                SectionTitle("Quick Access")
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val row1 = listOf("Play All Songs", "Shuffle All Music", "Open Playlists")
                    val row2 = listOf("Open Favorites", "Open Equalizer", "Open Queue", "Recently Played")
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row1.forEach { title ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color.White.copy(alpha = 0.1f))
                                    .clickable {
                                        when (title) {
                                            "Play All Songs" -> if (tracks.isNotEmpty()) viewModel.playPlaylist(tracks, contextName = "All Songs")
                                            "Shuffle All Music" -> if (tracks.isNotEmpty()) {
                                                viewModel.toggleShuffle()
                                                viewModel.playPlaylist(tracks, contextName = "All Songs")
                                            }
                                        }
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row2.forEach { title ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color.White.copy(alpha = 0.1f))
                                    .clickable { 
                                        if (title == "Open Equalizer") {
                                            onOpenEqualizer()
                                        } else if (title == "Open Queue") {
                                            onOpenQueue()
                                        }
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(title, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE91E63).copy(alpha = 0.8f))
                        .clickable { onOpenStats() }
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Insights, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Your Music Insights", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Check your top songs, artists, and monthly recaps.", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        }
                    }
                }
            }
            
            if (tracks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No local audio files found.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                item {
                    SectionTitle("Continue Listening")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(recommended) { track ->
                            RecommendedCard(track, viewModel) {
                                viewModel.playTrack(track, recommended, contextName = "Recommended")
                            }
                        }
                    }
                }

                item {
                    SectionTitle("Most Played Songs")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(recentlyPlayed) { track ->
                            RecommendedCard(track, viewModel) {
                                viewModel.playTrack(track, recentlyPlayed, contextName = "Recently Played")
                            }
                        }
                    }
                }

                item {
                    SectionTitle("Recently Added")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(recentlyPlayed) { track ->
                            RecommendedCard(track, viewModel) {
                                viewModel.playTrack(track, recentlyPlayed, contextName = "Recently Added")
                            }
                        }
                    }
                }

                item {
                    SectionTitle("Favorites")
                    val favs = tracks.filter { viewModel.isFavorite(it) }.take(10)
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(favs) { track ->
                            RecommendedCard(track, viewModel) {
                                viewModel.playTrack(track, favs, contextName = "Favorites")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        )
        Box {
            IconButton(onClick = { expanded = true }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.MoreVert, contentDescription = "Section Options", tint = Color.White.copy(alpha = 0.6f))
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                DropdownMenuItem(text = { Text("Hide Section", color = Color.White) }, onClick = { expanded = false })
                DropdownMenuItem(text = { Text("Move Up", color = Color.White) }, onClick = { expanded = false })
                DropdownMenuItem(text = { Text("Move Down", color = Color.White) }, onClick = { expanded = false })
                DropdownMenuItem(text = { Text("Pin Section", color = Color.White) }, onClick = { expanded = false })
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun RecommendedCard(track: AudioTrack, viewModel: MusicViewModel, onClick: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showSongInfoDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showPlaylistDialog) {
        AddToPlaylistDialog(
            track = track,
            viewModel = viewModel,
            onDismiss = { showPlaylistDialog = false }
        )
    }

    if (showSongInfoDialog) {
        SongInfoDialog(
            track = track,
            onDismiss = { showSongInfoDialog = false }
        )
    }

    if (showDeleteConfirmDialog) {
        DeleteConfirmDialog(
            track = track,
            viewModel = viewModel,
            onDismiss = { showDeleteConfirmDialog = false }
        )
    }

    Box(
        modifier = Modifier
            .width(140.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                AsyncImage(
                    model = track.albumArtUri,
                    contentDescription = "Album Art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Duration overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(track.durationMs)
                    val seconds = java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(track.durationMs) - java.util.concurrent.TimeUnit.MINUTES.toSeconds(minutes)
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = track.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (track.album.isNotEmpty() && track.album != "Unknown Album") track.album else track.artist,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                DropdownMenuItem(
                    text = { Text("Play", color = Color.White) },
                    onClick = { onClick(); showMenu = false }
                )
                DropdownMenuItem(
                    text = { Text("Play Next", color = Color.White) },
                    onClick = { viewModel.playNext(track); showMenu = false }
                )
                DropdownMenuItem(
                    text = { Text("Add to Queue", color = Color.White) },
                    onClick = { viewModel.addToQueue(track); showMenu = false }
                )
                DropdownMenuItem(
                    text = { Text("Add to Playlist", color = Color.White) },
                    onClick = { showPlaylistDialog = true; showMenu = false }
                )
                DropdownMenuItem(
                    text = { Text(if (viewModel.isFavorite(track)) "Remove from Favorites" else "Favorite", color = Color.White) },
                    onClick = { viewModel.toggleFavorite(track); showMenu = false }
                )
                DropdownMenuItem(
                    text = { Text("Share", color = Color.White) },
                    onClick = { viewModel.shareTrack(track); showMenu = false }
                )
                DropdownMenuItem(
                    text = { Text("View Details", color = Color.White) },
                    onClick = { showSongInfoDialog = true; showMenu = false }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = Color.Red) },
                    onClick = { showDeleteConfirmDialog = true; showMenu = false }
                )
            }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun TrackListItem(track: AudioTrack, viewModel: MusicViewModel, playlistName: String? = null, onClick: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showEditTagsDialog by remember { mutableStateOf(false) }
    var showSongInfoDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showPlaylistDialog) {
        AddToPlaylistDialog(
            track = track,
            viewModel = viewModel,
            onDismiss = { showPlaylistDialog = false }
        )
    }

    if (showEditTagsDialog) {
        EditTagsDialog(
            track = track,
            viewModel = viewModel,
            onDismiss = { showEditTagsDialog = false }
        )
    }

    if (showSongInfoDialog) {
        SongInfoDialog(
            track = track,
            onDismiss = { showSongInfoDialog = false }
        )
    }

    if (showDeleteConfirmDialog) {
        DeleteConfirmDialog(
            track = track,
            viewModel = viewModel,
            onDismiss = { showDeleteConfirmDialog = false }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            )
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            AsyncImage(
                model = track.albumArtUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = track.artist,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        val isMostPlayed = playlistName?.contains("Most Played", ignoreCase = true) == true
        if (isMostPlayed) {
            val playCount = viewModel.getPlayCount(track.id)
            Text(
                text = "$playCount plays",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = Color.White.copy(alpha = 0.7f))
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                DropdownMenuItem(
                    text = { Text("Play", color = Color.White) },
                    onClick = { 
                        onClick()
                        showMenu = false 
                    }
                )
                DropdownMenuItem(
                    text = { Text("Play Next", color = Color.White) },
                    onClick = { 
                        viewModel.playNext(track)
                        showMenu = false 
                    }
                )
                DropdownMenuItem(
                    text = { Text("Add to Queue", color = Color.White) },
                    onClick = { 
                        viewModel.addToQueue(track)
                        showMenu = false 
                    }
                )
                DropdownMenuItem(
                    text = { Text("Add to Playlist", color = Color.White) },
                    onClick = { 
                        showPlaylistDialog = true
                        showMenu = false 
                    }
                )
                if (playlistName != null) {
                    DropdownMenuItem(
                        text = { Text("Remove from Playlist", color = Color.Red) },
                        onClick = { 
                            viewModel.removeFromPlaylist(track, playlistName)
                            showMenu = false 
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text(if (viewModel.isFavorite(track)) "Remove from Favorites" else "Favorite", color = Color.White) },
                    onClick = { 
                        viewModel.toggleFavorite(track)
                        showMenu = false 
                    }
                )
                DropdownMenuItem(
                    text = { Text("Share", color = Color.White) },
                    onClick = { viewModel.shareTrack(track); showMenu = false }
                )
                DropdownMenuItem(
                    text = { Text("Edit Tags", color = Color.White) },
                    onClick = { showEditTagsDialog = true; showMenu = false }
                )
                DropdownMenuItem(
                    text = { Text("Song Information", color = Color.White) },
                    onClick = { showSongInfoDialog = true; showMenu = false }
                )
                DropdownMenuItem(
                    text = { Text("Set as Ringtone", color = Color.White) },
                    onClick = { viewModel.setAsRingtone(track); showMenu = false }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = Color.Red) },
                    onClick = { showDeleteConfirmDialog = true; showMenu = false }
                )
            }
        }
    }
}
