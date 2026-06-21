package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AudioTrack
import com.example.ui.theme.GradientBottom
import com.example.ui.theme.getThemeSingleColor
import com.example.ui.theme.getThemeGradientColors
import androidx.compose.ui.graphics.Brush
import com.example.viewmodel.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: MusicViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    val allTracks = viewModel.tracks.collectAsState().value
    var searchActive by remember { mutableStateOf(false) }
    var activeFilter by remember { mutableStateOf("All") }

    val searchResults = if (searchQuery.isBlank()) emptyList() else allTracks.filter { 
        val matchesQuery = it.title.contains(searchQuery, ignoreCase = true) || 
                           it.artist.contains(searchQuery, ignoreCase = true) ||
                           it.album.contains(searchQuery, ignoreCase = true)
        
        val matchesFilter = when (activeFilter) {
            "Songs" -> it.title.contains(searchQuery, ignoreCase = true)
            "Artists" -> it.artist.contains(searchQuery, ignoreCase = true)
            "Albums" -> it.album.contains(searchQuery, ignoreCase = true)
            else -> true
        }
        matchesQuery && matchesFilter
    }
    
    val recentSearches by viewModel.recentSearches.collectAsState()

    val themeMode by viewModel.themeMode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = getThemeGradientColors(themeMode)
                )
            )
    ) {
        // Search Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it; searchActive = it.isNotEmpty() },
                placeholder = { Text("Search songs, artists, albums...", color = Color.White.copy(alpha = 0.5f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White.copy(alpha = 0.5f)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = ""; searchActive = false }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.5f))
                        }
                    } else {
                        IconButton(onClick = { /* Voice search placeholder */ }) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice Search", tint = Color.White.copy(alpha = 0.5f))
                        }
                    }
                },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { /* Filters placeholder */ }) {
                Icon(Icons.Default.Tune, contentDescription = "Filters", tint = Color.White)
            }
        }

        // Quick Filters horizontally scrollable
        if (searchActive) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Songs", "Albums", "Artists").forEach { filter ->
                    val isSelected = activeFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { activeFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = Color.White.copy(alpha = 0.05f),
                            labelColor = Color.White,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp)
        ) {
            if (!searchActive) {
                // Empty state
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Recent Searches", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = "Clear All", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, modifier = Modifier.clickable { viewModel.clearRecentSearches() })
                    }
                }
                items(recentSearches) { recent ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).clickable { searchQuery = recent; searchActive = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = recent, color = Color.White, modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.removeRecentSearch(recent) }) { Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White.copy(alpha = 0.5f)) }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "Suggested Searches", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 16.dp))
                }
                
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SuggestionCard(title = "Favorite Albums", modifier = Modifier.weight(1f), color = Color(0xFFE91E63))
                        SuggestionCard(title = "Popular Genres", modifier = Modifier.weight(1f), color = Color(0xFF9C27B0))
                    }
                }
            } else {
                if (searchResults.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(64.dp), contentAlignment = Alignment.Center) {
                            Text("No results found for \"$searchQuery\"", color = Color.White.copy(alpha = 0.5f))
                        }
                    }
                } else {
                    item {
                        Text(text = "Songs", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(vertical = 16.dp))
                    }
                    items(searchResults) { track ->
                        SearchResultTrackItem(track = track, viewModel = viewModel, searchResults = searchResults, query = searchQuery)
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestionCard(title: String, modifier: Modifier = Modifier, color: Color) {
    Box(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.3f))
            .clickable { }
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SearchResultTrackItem(track: AudioTrack, viewModel: MusicViewModel, searchResults: List<AudioTrack>, query: String) {
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
            .clip(RoundedCornerShape(8.dp))
            .clickable { 
                if (query.isNotBlank()) {
                    viewModel.addRecentSearch(query)
                }
                viewModel.playTrack(track, searchResults, contextName = "Search Results") 
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = track.albumArtUri,
                contentDescription = "Album Art",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                error = androidx.compose.ui.graphics.vector.rememberVectorPainter(image = Icons.Default.MusicNote)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = track.title, color = Color.White, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = track.artist, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.White.copy(alpha = 0.5f))
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                DropdownMenuItem(text = { Text("Play", color = Color.White) }, onClick = { viewModel.playTrack(track, searchResults, "Search"); showMenu = false })
                DropdownMenuItem(text = { Text("Play Next", color = Color.White) }, onClick = { viewModel.playNext(track); showMenu = false })
                DropdownMenuItem(text = { Text("Add to Queue", color = Color.White) }, onClick = { viewModel.addToQueue(track); showMenu = false })
                DropdownMenuItem(text = { Text("Add to Playlist", color = Color.White) }, onClick = { showPlaylistDialog = true; showMenu = false })
                DropdownMenuItem(text = { Text(if (viewModel.isFavorite(track)) "Remove from Favorites" else "Favorite", color = Color.White) }, onClick = { viewModel.toggleFavorite(track); showMenu = false })
                DropdownMenuItem(text = { Text("Share", color = Color.White) }, onClick = { viewModel.shareTrack(track); showMenu = false })
                DropdownMenuItem(text = { Text("Edit Tags", color = Color.White) }, onClick = { showEditTagsDialog = true; showMenu = false })
                DropdownMenuItem(text = { Text("Song Information", color = Color.White) }, onClick = { showSongInfoDialog = true; showMenu = false })
                DropdownMenuItem(text = { Text("Set as Ringtone", color = Color.White) }, onClick = { viewModel.setAsRingtone(track); showMenu = false })
                DropdownMenuItem(text = { Text("Delete", color = Color.Red) }, onClick = { showDeleteConfirmDialog = true; showMenu = false })
            }
        }
    }
}
