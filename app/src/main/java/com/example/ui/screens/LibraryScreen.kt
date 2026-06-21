package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(viewModel: MusicViewModel) {
    val tracks = viewModel.tracks.collectAsState().value
    val favorites = viewModel.favorites.collectAsState().value
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedAlbum by remember { mutableStateOf<List<AudioTrack>?>(null) }
    var isAlbumGridView by remember { mutableStateOf(false) }
    var isSongsGridView by remember { mutableStateOf(false) }
    var sortOptionsExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val tabs = listOf("Songs", "Albums", "Artists", "Genres", "Playlists", "Folders", "Composers", "Years", "Favorites", "Recently Added", "Recently Played")

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
        if (selectedAlbum != null) {
            AlbumDetailsScreen(
                albumTracks = selectedAlbum!!,
                viewModel = viewModel,
                onBack = { selectedAlbum = null }
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Library",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Row {
                        if (selectedTabIndex == 0) {
                            Box {
                                IconButton(onClick = { sortOptionsExpanded = true }) {
                                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort", tint = Color.White)
                                }
                                DropdownMenu(
                                    expanded = sortOptionsExpanded,
                                    onDismissRequest = { sortOptionsExpanded = false },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    val sortOptions = listOf("Name", "Artist", "Album", "Duration", "Date Added", "Play Count", "Last Played", "File Size", "Bitrate")
                                    sortOptions.forEach { option ->
                                        DropdownMenuItem(text = { Text(option, color = Color.White) }, onClick = { sortOptionsExpanded = false })
                                    }
                                }
                            }
                            IconButton(onClick = { isSongsGridView = !isSongsGridView }) {
                                Icon(
                                    imageVector = if (isSongsGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                                    contentDescription = "Toggle View",
                                    tint = Color.White
                                )
                            }
                        }
                        if (selectedTabIndex == 1) {
                            IconButton(onClick = { isAlbumGridView = !isAlbumGridView }) {
                                Icon(
                                    imageVector = if (isAlbumGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                                    contentDescription = "Toggle View",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter...", color = Color.White.copy(alpha = 0.5f)) },
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
            
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 16.dp,
                modifier = Modifier.padding(bottom = 8.dp),
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                title,
                                color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f),
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
            
            when (selectedTabIndex) {
                0 -> {
                    val displayedTracks = if (searchQuery.isBlank()) tracks else tracks.filter { it.title.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true) }
                    
                    if (isSongsGridView) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            gridItems(displayedTracks) { track ->
                                AlbumGridItem(track = track, onClick = { viewModel.playTrack(track, displayedTracks, contextName = "All Songs") })
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(displayedTracks) { track ->
                                TrackListItem(track = track, viewModel = viewModel, onClick = { viewModel.playTrack(track, displayedTracks, contextName = "All Songs") })
                            }
                        }
                    }
                }
                1 -> {
                    // Albums
                    val albums = tracks.groupBy { it.album }.map { it.value }
                    val displayedAlbums = if (searchQuery.isBlank()) albums else albums.filter { it.first().album.contains(searchQuery, ignoreCase = true) || it.first().artist.contains(searchQuery, ignoreCase = true) }
                    
                    if (isAlbumGridView) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            gridItems(displayedAlbums) { albumTracks ->
                                AlbumGridItem(track = albumTracks.first(), onClick = { selectedAlbum = albumTracks })
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(displayedAlbums) { albumTracks ->
                                AlbumListItem(track = albumTracks.first(), onClick = { selectedAlbum = albumTracks })
                            }
                        }
                    }
                }
                2 -> {
                    // Artists
                    val artists = tracks.groupBy { it.artist }.map { it.value }
                    val displayedArtists = if (searchQuery.isBlank()) artists else artists.filter { it.first().artist.contains(searchQuery, ignoreCase = true) }
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(displayedArtists) { artistTracks ->
                            val albumCount = artistTracks.distinctBy { it.album }.size
                            CategoryListItem(title = artistTracks.first().artist, subtitle = "${artistTracks.size} Songs • ${albumCount} Albums", onClick = { viewModel.playPlaylist(artistTracks, contextName = "Artist: ${artistTracks.first().artist}") })
                        }
                    }
                }
                3 -> {
                    // Genres
                    val genres = tracks.groupBy { it.genre }.map { it.value }
                    val displayedGenres = if (searchQuery.isBlank()) genres else genres.filter { it.first().genre.contains(searchQuery, ignoreCase = true) }
                    LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(displayedGenres) { tracksList ->
                            CategoryListItem(title = tracksList.first().genre, subtitle = "${tracksList.size} Songs", onClick = { viewModel.playPlaylist(tracksList, contextName = "Genre: ${tracksList.first().genre}") })
                        }
                    }
                }
                4 -> {
                    // Playlists - redirect to generic text
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Use the Playlists tab in Bottom Nav", color = Color.White.copy(alpha = 0.6f))
                    }
                }
                5 -> {
                    // Folders
                    val folders = tracks.groupBy { it.folder }.map { it.value }
                    val displayedFolders = if (searchQuery.isBlank()) folders else folders.filter { it.first().folder.contains(searchQuery, ignoreCase = true) }
                    LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(displayedFolders) { tracksList ->
                            CategoryListItem(title = tracksList.first().folder, subtitle = "${tracksList.size} Files", onClick = { viewModel.playPlaylist(tracksList, contextName = "Folder: ${tracksList.first().folder}") })
                        }
                    }
                }
                6 -> {
                    // Composers
                    val composers = tracks.groupBy { it.composer }.map { it.value }
                    val displayedComposers = if (searchQuery.isBlank()) composers else composers.filter { it.first().composer.contains(searchQuery, ignoreCase = true) }
                    LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(displayedComposers) { tracksList ->
                            CategoryListItem(title = tracksList.first().composer, subtitle = "${tracksList.size} Songs", onClick = { viewModel.playPlaylist(tracksList, contextName = "Composer: ${tracksList.first().composer}") })
                        }
                    }
                }
                7 -> {
                    // Years
                    val years = tracks.groupBy { it.year }.map { it.value }
                    val displayedYears = if (searchQuery.isBlank()) years else years.filter { it.first().year.contains(searchQuery, ignoreCase = true) }
                    LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(displayedYears) { tracksList ->
                            CategoryListItem(title = tracksList.first().year, subtitle = "${tracksList.size} Songs", onClick = { viewModel.playPlaylist(tracksList, contextName = "Year: ${tracksList.first().year}") })
                        }
                    }
                }
                8 -> {
                    // Favorites
                    val displayedFavorites = if (searchQuery.isBlank()) favorites else favorites.filter { it.title.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true) }
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(displayedFavorites) { track ->
                            TrackListItem(track = track, viewModel = viewModel, onClick = { viewModel.playTrack(track, displayedFavorites, contextName = "Favorites") })
                        }
                    }
                }
                9 -> {
                    // Recently Added
                    val recentAdded = tracks.take(20)
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(recentAdded) { track ->
                            TrackListItem(track = track, viewModel = viewModel, onClick = { viewModel.playTrack(track, recentAdded, contextName = "Recently Added") })
                        }
                    }
                }
                10 -> {
                    // Recently Played
                    val recentPlayed = tracks.take(15).shuffled()
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(recentPlayed) { track ->
                            TrackListItem(track = track, viewModel = viewModel, onClick = { viewModel.playTrack(track, recentPlayed, contextName = "Recently Played") })
                        }
                    }
                }
            }
        }
        
        var showFabMenu by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            FloatingActionButton(
                onClick = { showFabMenu = true },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 72.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Quick Actions", tint = Color.White)
            }
            DropdownMenu(
                expanded = showFabMenu,
                onDismissRequest = { showFabMenu = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant).align(Alignment.BottomEnd)
            ) {
                DropdownMenuItem(text = { Text("Create Playlist", color = Color.White) }, onClick = { showFabMenu = false })
                DropdownMenuItem(text = { Text("Scan Music", color = Color.White) }, onClick = { showFabMenu = false })
                DropdownMenuItem(text = { Text("Import Music", color = Color.White) }, onClick = { showFabMenu = false })
                DropdownMenuItem(text = { Text("Open Queue", color = Color.White) }, onClick = { showFabMenu = false })
            }
        }
    }
}
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AlbumListItem(track: AudioTrack, onClick: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            )
            .background(Color.White.copy(alpha = 0.05f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.DarkGray)
        ) {
            AsyncImage(
                model = track.albumArtUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.album,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = track.artist,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
                DropdownMenuItem(text = { Text("Play Album", color = Color.White) }, onClick = { showMenu = false; onClick() })
                DropdownMenuItem(text = { Text("Shuffle Album", color = Color.White) }, onClick = { showMenu = false; onClick() })
                DropdownMenuItem(text = { Text("Add Album to Queue", color = Color.White) }, onClick = { showMenu = false })
                DropdownMenuItem(text = { Text("Add Album to Playlist", color = Color.White) }, onClick = { showMenu = false })
                DropdownMenuItem(text = { Text("Favorite Album", color = Color.White) }, onClick = { showMenu = false })
                DropdownMenuItem(text = { Text("Share Album", color = Color.White) }, onClick = { showMenu = false })
                DropdownMenuItem(text = { Text("Edit Album Tags", color = Color.White) }, onClick = { showMenu = false })
                DropdownMenuItem(text = { Text("Album Information", color = Color.White) }, onClick = { showMenu = false })
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CategoryListItem(title: String, subtitle: String? = null, onClick: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            )
            .background(Color.White.copy(alpha = 0.05f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(50)) // Circle
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title.take(1).uppercase(),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha=0.6f),
                    fontSize = 14.sp
                )
            }
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
                DropdownMenuItem(text = { Text("Play", color = Color.White) }, onClick = { showMenu = false; onClick() })
                DropdownMenuItem(text = { Text("Shuffle", color = Color.White) }, onClick = { showMenu = false; onClick() })
                DropdownMenuItem(text = { Text("Add to Queue", color = Color.White) }, onClick = { showMenu = false })
                DropdownMenuItem(text = { Text("Add to Playlist", color = Color.White) }, onClick = { showMenu = false })
                DropdownMenuItem(text = { Text("Rename", color = Color.White) }, onClick = { showMenu = false })
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AlbumGridItem(track: AudioTrack, onClick: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            )
            .background(Color.White.copy(alpha = 0.05f))
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.DarkGray)
        ) {
            AsyncImage(
                model = track.albumArtUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = track.album,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = track.artist,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            DropdownMenuItem(text = { Text("Play Album", color = Color.White) }, onClick = { showMenu = false; onClick() })
            DropdownMenuItem(text = { Text("Add Album to Queue", color = Color.White) }, onClick = { showMenu = false })
        }
    }
}

@Composable
fun AlbumDetailsScreen(albumTracks: List<AudioTrack>, viewModel: MusicViewModel, onBack: () -> Unit) {
    val track = albumTracks.first()
    var showMenu by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            AsyncImage(
                model = track.albumArtUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
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
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        DropdownMenuItem(text = { Text("Play Album", color = Color.White) }, onClick = { showMenu = false; viewModel.playPlaylist(albumTracks, contextName = "Album: ${track.album}") })
                        DropdownMenuItem(text = { Text("Shuffle Album", color = Color.White) }, onClick = { showMenu = false; viewModel.playPlaylist(albumTracks.shuffled(), contextName = "Album: ${track.album}") })
                        DropdownMenuItem(text = { Text("Add Album to Queue", color = Color.White) }, onClick = { showMenu = false })
                        DropdownMenuItem(text = { Text("Add Album to Playlist", color = Color.White) }, onClick = { showMenu = false })
                        DropdownMenuItem(text = { Text("Favorite Album", color = Color.White) }, onClick = { showMenu = false })
                        DropdownMenuItem(text = { Text("Share Album", color = Color.White) }, onClick = { showMenu = false })
                        DropdownMenuItem(text = { Text("Edit Album Tags", color = Color.White) }, onClick = { showMenu = false })
                        DropdownMenuItem(text = { Text("Album Information", color = Color.White) }, onClick = { showMenu = false })
                    }
                }
            }
            // Album Info
            Column(
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
            ) {
                Text(
                    text = track.album,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "${track.artist} • ${albumTracks.size} songs",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            FloatingActionButton(
                onClick = { viewModel.playPlaylist(albumTracks, contextName = "Album: ${track.album}") },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play Album", tint = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(albumTracks) { t ->
                TrackListItem(track = t, viewModel = viewModel, onClick = { viewModel.playTrack(t, albumTracks, contextName = "Album: ${track.album}") })
            }
        }
    }
}
