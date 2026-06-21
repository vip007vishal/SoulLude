package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
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
fun QueueScreen(viewModel: MusicViewModel, onBack: () -> Unit) {
    val queue = viewModel.queue.collectAsState().value
    val queues = viewModel.queues.collectAsState().value
    val activeQueueId = viewModel.activeQueueId.collectAsState().value
    val currentTrack = viewModel.currentTrack.collectAsState().value
    
    var searchQuery by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    
    var showSaveDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDuplicateDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showConvertDialog by remember { mutableStateOf(false) }
    var showStatsDialog by remember { mutableStateOf(false) }

    val filteredQueue = if (searchQuery.isBlank()) queue else queue.filter { 
        it.title.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true) 
    }

    // Dialogs Implementation
    if (showSaveDialog) {
        var newQueueName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Current Queue") },
            text = {
                OutlinedTextField(
                    value = newQueueName,
                    onValueChange = { newQueueName = it },
                    label = { Text("Queue Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newQueueName.isNotBlank()) {
                            viewModel.createQueue(newQueueName, queue)
                            showSaveDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRenameDialog) {
        val currentQueue = queues.find { it.id == activeQueueId }
        var queueName by remember { mutableStateOf(currentQueue?.name ?: "") }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Queue") },
            text = {
                OutlinedTextField(
                    value = queueName,
                    onValueChange = { queueName = it },
                    label = { Text("New Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (queueName.isNotBlank()) {
                            viewModel.renameQueue(activeQueueId, queueName)
                            showRenameDialog = false
                        }
                    }
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDuplicateDialog) {
        val currentQueue = queues.find { it.id == activeQueueId }
        var copyName by remember { mutableStateOf("${currentQueue?.name ?: ""} (Copy)") }
        AlertDialog(
            onDismissRequest = { showDuplicateDialog = false },
            title = { Text("Duplicate Queue") },
            text = {
                OutlinedTextField(
                    value = copyName,
                    onValueChange = { copyName = it },
                    label = { Text("Duplicate Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (copyName.isNotBlank()) {
                            viewModel.duplicateQueue(activeQueueId, copyName)
                            showDuplicateDialog = false
                        }
                    }
                ) {
                    Text("Duplicate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDuplicateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showExportDialog) {
        val exportData = viewModel.exportQueue(activeQueueId)
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Queue") },
            text = {
                Column {
                    Text("Copy the following text to backup or share this queue:", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exportData,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showExportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showImportDialog) {
        var importData by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Queue") },
            text = {
                OutlinedTextField(
                    value = importData,
                    onValueChange = { importData = it },
                    placeholder = { Text("Paste exported queue data here...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importData.isNotBlank()) {
                            val newId = viewModel.restoreQueue(importData)
                            if (newId != null) {
                                viewModel.switchQueue(newId, playWhenReady = true)
                            }
                            showImportDialog = false
                        }
                    }
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showConvertDialog) {
        var playlistName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showConvertDialog = false },
            title = { Text("Convert to Playlist") },
            text = {
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    label = { Text("Playlist Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (playlistName.isNotBlank()) {
                            viewModel.convertQueueToPlaylist(activeQueueId, playlistName)
                            showConvertDialog = false
                        }
                    }
                ) {
                    Text("Convert")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConvertDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showStatsDialog) {
        val totalMs = queue.sumOf { it.durationMs }
        val totalSec = totalMs / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        val avgMs = if (queue.isNotEmpty()) totalMs / queue.size else 0L
        val avgSec = avgMs / 1000
        val avgMin = avgSec / 60
        val avgSecRemainder = avgSec % 60
        AlertDialog(
            onDismissRequest = { showStatsDialog = false },
            title = { Text("Queue Statistics") },
            text = {
                Column {
                    Text("Total Songs: ${queue.size}", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Total Playtime: ${min}m ${sec}s", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Average Duration: ${avgMin}m ${avgSecRemainder}s", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            },
            confirmButton = {
                Button(onClick = { showStatsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    val themeMode by viewModel.themeMode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = getThemeGradientColors(themeMode)
                )
            )
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Column(modifier = Modifier.weight(1f)) {
                val currentQueue = queues.find { it.id == activeQueueId }
                Text(currentQueue?.name ?: "Current Queue", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("${queue.size} songs", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                DropdownMenuItem(text = { Text("Save Queue", color = Color.White) }, onClick = { showMenu = false; showSaveDialog = true })
                DropdownMenuItem(text = { Text("Rename Queue", color = Color.White) }, onClick = { showMenu = false; showRenameDialog = true })
                DropdownMenuItem(text = { Text("Duplicate Queue", color = Color.White) }, onClick = { showMenu = false; showDuplicateDialog = true })
                DropdownMenuItem(text = { Text("Clear Queue", color = Color.White) }, onClick = { showMenu = false; viewModel.clearQueue(activeQueueId) })
                DropdownMenuItem(text = { Text("Export Queue", color = Color.White) }, onClick = { showMenu = false; showExportDialog = true })
                DropdownMenuItem(text = { Text("Import Queue", color = Color.White) }, onClick = { showMenu = false; showImportDialog = true })
                DropdownMenuItem(text = { Text("Convert Queue to Playlist", color = Color.White) }, onClick = { showMenu = false; showConvertDialog = true })
                DropdownMenuItem(text = { Text("Queue Statistics", color = Color.White) }, onClick = { showMenu = false; showStatsDialog = true })
                DropdownMenuItem(text = { Text("Delete Queue", color = Color.Red) }, onClick = { showMenu = false; viewModel.deleteQueue(activeQueueId) })
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search in queue...", color = Color.White.copy(alpha = 0.5f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White.copy(alpha = 0.5f)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.5f))
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).height(50.dp),
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
        
        // Quick Actions
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { viewModel.shuffleQueue(activeQueueId) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text("Shuffle", color = Color.White)
            }
            Button(onClick = { viewModel.sortQueue(activeQueueId) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text("Sort", color = Color.White)
            }
            Button(onClick = { showSaveDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text("Save", color = Color.White)
            }
            Button(onClick = { viewModel.clearQueue(activeQueueId) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text("Clear", color = Color.White)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            itemsIndexed(filteredQueue) { index, track ->
                QueueTrackItem(
                    track = track,
                    viewModel = viewModel,
                    activeQueueId = activeQueueId,
                    isCurrent = track == currentTrack,
                    index = index + 1
                )
            }
        }
    }
}

@Composable
fun QueueTrackItem(track: AudioTrack, viewModel: MusicViewModel, activeQueueId: String, isCurrent: Boolean, index: Int) {
    var showMenu by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }

    if (showPlaylistDialog) {
        AddToPlaylistDialog(
            track = track,
            viewModel = viewModel,
            onDismiss = { showPlaylistDialog = false }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
            .clickable { viewModel.playQueueTrack(track, index - 1) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isCurrent) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Playing", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp).padding(end = 8.dp))
        } else {
            Text(text = index.toString(), color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, modifier = Modifier.width(24.dp).padding(end = 8.dp))
        }

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
            Text(text = track.title, color = if (isCurrent) MaterialTheme.colorScheme.primary else Color.White, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = track.artist, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(text = formatQueueTime(track.durationMs), color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, modifier = Modifier.padding(end = 8.dp))

        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.White)
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                DropdownMenuItem(text = { Text("Play Now", color = Color.White) }, onClick = { viewModel.playQueueTrack(track, index - 1); showMenu = false })
                DropdownMenuItem(text = { Text("Play Next", color = Color.White) }, onClick = { viewModel.playNext(listOf(track)); showMenu = false })
                DropdownMenuItem(text = { Text("Move to Top", color = Color.White) }, onClick = { viewModel.moveQueueTrackToTop(activeQueueId, index - 1); showMenu = false })
                DropdownMenuItem(text = { Text("Move to Bottom", color = Color.White) }, onClick = { viewModel.moveQueueTrackToBottom(activeQueueId, index - 1); showMenu = false })
                DropdownMenuItem(text = { Text("Add to Playlist", color = Color.White) }, onClick = { showPlaylistDialog = true; showMenu = false })
                DropdownMenuItem(text = { Text("Favorite", color = Color.White) }, onClick = { viewModel.toggleFavorite(track); showMenu = false })
                DropdownMenuItem(text = { Text("Remove from Queue", color = Color.Red) }, onClick = { viewModel.removeQueueTrack(activeQueueId, index - 1); showMenu = false })
            }
        }
    }
}

private fun formatQueueTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
