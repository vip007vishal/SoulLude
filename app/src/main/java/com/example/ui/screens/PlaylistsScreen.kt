package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ViewCompact
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
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
fun PlaylistsScreen(viewModel: MusicViewModel) {
    val recentlyPlayed = viewModel.recentlyPlayed.collectAsState().value
    val mostPlayed = viewModel.mostPlayed.collectAsState().value
    var selectedPlaylist by remember { mutableStateOf<Pair<String, List<AudioTrack>>?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableIntStateOf(0) }
    var viewMode by remember { mutableIntStateOf(0) } // 0 = List, 1 = Grid, 2 = Compact
    val categories = listOf("All Playlists", "Queues", "Favorites", "Smart Playlists", "Recently Created", "Recently Updated", "Pinned Playlists", "Playlist Folders")

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                viewModel.createPlaylist(name)
                showCreateDialog = false
            }
        )
    }

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
        if (selectedPlaylist != null) {
            PlaylistDetailsScreen(
                title = selectedPlaylist!!.first,
                tracks = selectedPlaylist!!.second,
                viewModel = viewModel,
                onBack = { selectedPlaylist = null }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 100.dp)
            ) {
                // Top Search Bar and Create Button
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search playlists...", color = Color.White.copy(alpha = 0.5f)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White.copy(alpha = 0.5f)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.5f))
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(24.dp),
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
                    IconButton(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(25.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create Playlist", tint = Color.White)
                    }
                }

                // Categories
                ScrollableTabRow(
                    selectedTabIndex = selectedCategory,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 16.dp,
                    divider = {}
                ) {
                    categories.forEachIndexed { index, category ->
                        Tab(
                            selected = selectedCategory == index,
                            onClick = { selectedCategory = index },
                            text = { 
                                Text(
                                    category, 
                                    color = if (selectedCategory == index) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f),
                                    fontWeight = if (selectedCategory == index) FontWeight.Bold else FontWeight.Normal
                                ) 
                            }
                        )
                    }
                }
                
                // View Mode Toggles
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { viewMode = 0 }) {
                        Icon(Icons.AutoMirrored.Filled.ViewList, contentDescription = "List View", tint = if (viewMode == 0) MaterialTheme.colorScheme.primary else Color.White.copy(alpha=0.6f))
                    }
                    IconButton(onClick = { viewMode = 1 }) {
                        Icon(Icons.Default.GridView, contentDescription = "Grid View", tint = if (viewMode == 1) MaterialTheme.colorScheme.primary else Color.White.copy(alpha=0.6f))
                    }
                    IconButton(onClick = { viewMode = 2 }) {
                        Icon(Icons.Default.ViewCompact, contentDescription = "Compact View", tint = if (viewMode == 2) MaterialTheme.colorScheme.primary else Color.White.copy(alpha=0.6f))
                    }
                }

                if (selectedCategory == 3) {
                    if (recentlyPlayed.isNotEmpty()) {
                        PlaylistSection("Recently Played (Smart)", recentlyPlayed, viewModel) {
                            selectedPlaylist = "Recently Played (Smart)" to recentlyPlayed
                        }
                    }

                    if (mostPlayed.isNotEmpty()) {
                        PlaylistSection("Most Played (Smart)", mostPlayed, viewModel) {
                            selectedPlaylist = "Most Played (Smart)" to mostPlayed
                        }
                    }
                }

                if (selectedCategory == 1) {
                    val queues = viewModel.queues.collectAsState().value
                    val filteredQueues = if (searchQuery.isBlank()) queues else queues.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    
                    if (filteredQueues.isNotEmpty()) {
                        filteredQueues.forEach { queue ->
                            PlaylistListItem(
                                name = queue.name.ifEmpty { "Queue ${queue.id.takeLast(4)}" },
                                tracks = queue.tracks,
                                viewModel = viewModel,
                                isCompact = (viewMode == 2),
                                onClick = { selectedPlaylist = queue.name.ifEmpty { "Queue" } to queue.tracks }
                            )
                        }
                    } else {
                         Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                             Text("No queues yet.", color = Color.White.copy(alpha = 0.5f))
                         }
                    }
                } else {
                    val customPlaylists = viewModel.customPlaylists.collectAsState().value
                    val filteredPlaylists = if (searchQuery.isBlank()) customPlaylists else customPlaylists.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    
                    if (filteredPlaylists.isNotEmpty()) {
                        if (viewMode == 1) {
                            // Grid View (Simplified as Row chunks for LazyColumn/VerticalScroll compatibility without LazyVerticalGrid for simplicity or we can just iterate)
                            for (i in filteredPlaylists.indices step 2) {
                                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        PlaylistGridItem(name = filteredPlaylists[i].name, tracks = filteredPlaylists[i].tracks, viewModel = viewModel, onClick = { selectedPlaylist = filteredPlaylists[i].name to filteredPlaylists[i].tracks })
                                    }
                                    if (i + 1 < filteredPlaylists.size) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            PlaylistGridItem(name = filteredPlaylists[i+1].name, tracks = filteredPlaylists[i+1].tracks, viewModel = viewModel, onClick = { selectedPlaylist = filteredPlaylists[i+1].name to filteredPlaylists[i+1].tracks })
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        } else {
                            filteredPlaylists.forEach { playlist ->
                                PlaylistListItem(
                                    name = playlist.name,
                                    tracks = playlist.tracks,
                                    viewModel = viewModel,
                                    isCompact = (viewMode == 2),
                                    onClick = { selectedPlaylist = playlist.name to playlist.tracks }
                                )
                            }
                        }
                    } else if (customPlaylists.isEmpty() && selectedCategory != 3) {
                         Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                             Text("No playlists yet. Create one!", color = Color.White.copy(alpha = 0.5f))
                         }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistSection(title: String, tracks: List<AudioTrack>, viewModel: MusicViewModel, onViewAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
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
        Text(
            text = "View All",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp,
            modifier = Modifier.clickable { onViewAll() }.padding(4.dp)
        )
    }

    val displayTracks = tracks.take(10)
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(displayTracks) { track ->
            PlaylistTrackCard(track = track, viewModel = viewModel) {
                viewModel.playTrack(track, displayTracks, contextName = title)
            }
        }
    }
}

@Composable
fun PlaylistTrackCard(track: AudioTrack, viewModel: MusicViewModel, onClick: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }

    if (showPlaylistDialog) {
        AddToPlaylistDialog(
            track = track,
            viewModel = viewModel,
            onDismiss = { showPlaylistDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.DarkGray)
        ) {
            AsyncImage(
                model = track.albumArtUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp).clickable { showMenu = true }
                )
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
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
                    DropdownMenuItem(
                        text = { Text(if (viewModel.isFavorite(track)) "Remove from Favorites" else "Favorite", color = Color.White) },
                        onClick = { 
                            viewModel.toggleFavorite(track)
                            showMenu = false 
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename", color = Color.White) },
                        onClick = { showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        onClick = { showMenu = false }
                    )
                }
            }
        }
    }
}

@Composable
fun PlaylistListItem(name: String, tracks: List<AudioTrack>, viewModel: MusicViewModel, isCompact: Boolean = false, onClick: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showRenameDialog) {
        RenamePlaylistDialog(
            initialName = name,
            onDismiss = { showRenameDialog = false },
            onRename = { newName ->
                viewModel.renamePlaylist(name, newName)
                showRenameDialog = false
            }
        )
    }

    if (showDeleteDialog) {
        DeletePlaylistDialog(
            playlistName = name,
            onDismiss = { showDeleteDialog = false },
            onDelete = {
                viewModel.deletePlaylist(name)
                showDeleteDialog = false
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = if (isCompact) 4.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val firstTrack = tracks.firstOrNull()
        val size = if (isCompact) 48.dp else 64.dp
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            if (firstTrack != null) {
                AsyncImage(
                    model = firstTrack.albumArtUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Default.LibraryMusic, contentDescription = null, tint = Color.LightGray)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = if (isCompact) 14.sp else 16.sp)
            Text(text = "${tracks.size} songs", color = Color.White.copy(alpha = 0.6f), fontSize = if (isCompact) 12.sp else 14.sp)
        }
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = Color.LightGray)
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant).heightIn(max = 400.dp)
            ) {
                DropdownMenuItem(
                    text = { Text("Play All", color = Color.White) },
                    onClick = {
                        if (tracks.isNotEmpty()) {
                            viewModel.playPlaylist(tracks, contextName = "Playlist: $name")
                        }
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Shuffle Play", color = Color.White) },
                    onClick = {
                        if (tracks.isNotEmpty()) {
                            viewModel.playPlaylist(tracks.shuffled(), contextName = "Playlist: $name")
                        }
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Add to Queue", color = Color.White) },
                    onClick = {
                        viewModel.addAllToQueue(tracks)
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Play Next", color = Color.White) },
                    onClick = {
                        viewModel.playNext(tracks)
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Rename Playlist", color = Color.White) },
                    onClick = { 
                        showRenameDialog = true
                        showMenu = false 
                    }
                )
                DropdownMenuItem(
                    text = { Text("Duplicate Playlist", color = Color.White) },
                    onClick = {
                        viewModel.duplicatePlaylist(name, "$name (Copy)")
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete Playlist", color = Color.Red) },
                    onClick = {
                        showDeleteDialog = true
                        showMenu = false
                    }
                )
            }
        }
    }
}

@Composable
fun PlaylistGridItem(name: String, tracks: List<AudioTrack>, viewModel: MusicViewModel, onClick: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val firstTrack = tracks.firstOrNull()
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            if (firstTrack != null) {
                AsyncImage(
                    model = firstTrack.albumArtUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Default.LibraryMusic, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(32.dp))
            }
            
            Box(
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp).padding(4.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant).heightIn(max = 400.dp)
                ) {
                    DropdownMenuItem(text = { Text("Play All", color = Color.White) }, onClick = { showMenu = false; if (tracks.isNotEmpty()) viewModel.playPlaylist(tracks, contextName = "Playlist: $name") })
                    DropdownMenuItem(text = { Text("Rename", color = Color.White) }, onClick = { showMenu = false })
                    DropdownMenuItem(text = { Text("Delete", color = Color.Red) }, onClick = { showMenu = false })
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${tracks.size} songs",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun PlaylistDetailsScreen(title: String, tracks: List<AudioTrack>, viewModel: MusicViewModel, onBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddSongsDialog by remember { mutableStateOf(false) }
    var currentTitle by remember { mutableStateOf(title) }

    val playlists = viewModel.customPlaylists.collectAsState().value
    val playlistInfo = playlists.find { it.name == currentTitle }
    val currentTracks = playlistInfo?.tracks ?: tracks
    val activeFirstTrack = currentTracks.firstOrNull()

    if (showRenameDialog) {
        RenamePlaylistDialog(
            initialName = currentTitle,
            onDismiss = { showRenameDialog = false },
            onRename = { newName ->
                viewModel.renamePlaylist(currentTitle, newName)
                currentTitle = newName
                showRenameDialog = false
            }
        )
    }

    if (showDeleteDialog) {
        DeletePlaylistDialog(
            playlistName = currentTitle,
            onDismiss = { showDeleteDialog = false },
            onDelete = {
                viewModel.deletePlaylist(currentTitle)
                showDeleteDialog = false
                onBack()
            }
        )
    }

    if (showAddSongsDialog) {
        AddSongsToPlaylistDialog(
            playlistName = currentTitle,
            viewModel = viewModel,
            onDismiss = { showAddSongsDialog = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            if (activeFirstTrack != null) {
                AsyncImage(
                    model = activeFirstTrack.albumArtUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray)) {
                    Icon(Icons.Default.LibraryMusic, contentDescription = null, tint = Color.LightGray, modifier = Modifier.align(Alignment.Center).size(64.dp))
                }
            }
            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, GradientBottom),
                            startY = 100f
                        )
                    )
            )
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant).heightIn(max = 400.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text("Play All", color = Color.White) },
                        onClick = {
                            if (currentTracks.isNotEmpty()) {
                                viewModel.playPlaylist(currentTracks, contextName = "Playlist: $currentTitle")
                            }
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Shuffle Play", color = Color.White) },
                        onClick = {
                            if (currentTracks.isNotEmpty()) {
                                viewModel.playPlaylist(currentTracks.shuffled(), contextName = "Playlist: $currentTitle")
                            }
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add to Queue", color = Color.White) },
                        onClick = {
                            viewModel.addAllToQueue(currentTracks)
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Play Next", color = Color.White) },
                        onClick = {
                            viewModel.playNext(currentTracks)
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename Playlist", color = Color.White) },
                        onClick = { 
                            showRenameDialog = true
                            showMenu = false 
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Change Cover Image", color = Color.White) },
                        onClick = { showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Add Description", color = Color.White) },
                        onClick = { showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Sort / Reorder Songs", color = Color.White) },
                        onClick = { showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicate Playlist", color = Color.White) },
                        onClick = {
                            viewModel.duplicatePlaylist(currentTitle, "$currentTitle (Copy)")
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Merge Playlist", color = Color.White) },
                        onClick = { showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Export / Share & Backup", color = Color.White) },
                        onClick = { showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Pin to Home UI", color = Color.White) },
                        onClick = { showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Move to Folder", color = Color.White) },
                        onClick = { showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("View Statistics", color = Color.White) },
                        onClick = { showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Playlist", color = Color.Red) },
                        onClick = {
                            showDeleteDialog = true
                            showMenu = false
                        }
                    )
                }
            }

            // Playlist Info
            Column(
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
            ) {
                Text(
                    text = currentTitle,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "A collection of your favorite tracks.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
                val totalDurationStr = "00:00"
                Text(
                    text = "${currentTracks.size} songs • $totalDurationStr • Created: Jan 1 • Updated: Today",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.playPlaylist(currentTracks, contextName = "Playlist: $currentTitle") }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                        Text("Play", color = Color.White)
                    }
                    Button(onClick = { viewModel.playPlaylist(currentTracks.shuffled(), contextName = "Playlist: $currentTitle") }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Text("Shuffle", color = Color.White)
                    }
                    Button(onClick = { showAddSongsDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Text("Add Songs", color = Color.White)
                    }
                    Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Text("Edit", color = Color.White)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search in playlist...", color = Color.White.copy(alpha = 0.5f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White.copy(alpha = 0.5f)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.5f))
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 0.dp),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        val filteredTracks = if (searchQuery.isBlank()) currentTracks else currentTracks.filter { it.title.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true) }

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredTracks) { t ->
                TrackListItem(track = t, viewModel = viewModel, playlistName = currentTitle, onClick = { viewModel.playTrack(t, filteredTracks, contextName = "Playlist: $currentTitle") })
            }
        }
    }
}
