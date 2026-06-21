package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioTrack
import com.example.viewmodel.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistDialog(
    track: AudioTrack,
    viewModel: MusicViewModel,
    onDismiss: () -> Unit
) {
    var showCreateNew by remember { mutableStateOf(false) }

    if (showCreateNew) {
        CreatePlaylistDialog(
            onDismiss = { showCreateNew = false },
            onCreate = { playlistName ->
                viewModel.createPlaylist(playlistName)
                viewModel.addToPlaylist(track, playlistName)
                showCreateNew = false
                onDismiss()
            }
        )
    } else {
        val playlists = viewModel.getPlaylists()
        
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = "Add to Playlist", color = Color.White) },
            text = {
                Column {
                    Text(
                        text = "+ Create New Playlist",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCreateNew = true }
                            .padding(vertical = 12.dp)
                    )
                    
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(playlists) { playlistName ->
                            Text(
                                text = playlistName,
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.addToPlaylist(track, playlistName)
                                        onDismiss()
                                    }
                                    .padding(vertical = 12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Close", color = Color.White.copy(alpha = 0.7f))
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "New Playlist", color = Color.White) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Playlist Name") },
                singleLine = true,
                colors = run {
                    val colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                    )
                    colors
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) onCreate(name.trim())
                }
            ) {
                Text("Create", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
}

@Composable
fun RenamePlaylistDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Rename Playlist", color = Color.White) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Playlist Name") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && name != initialName) onRename(name.trim())
                }
            ) {
                Text("Rename", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
}

@Composable
fun DeletePlaylistDialog(
    playlistName: String,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Playlist", color = Color.White) },
        text = { Text("Are you sure you want to delete '$playlistName'? This action cannot be undone.", color = Color.White.copy(alpha = 0.8f)) },
        confirmButton = {
            TextButton(onClick = onDelete) {
                Text("Delete", color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSongsToPlaylistDialog(
    playlistName: String,
    viewModel: MusicViewModel,
    onDismiss: () -> Unit
) {
    val tracks = viewModel.tracks.collectAsState().value
    var searchQuery by remember { mutableStateOf("") }
    val filteredTracks = if (searchQuery.isBlank()) tracks else tracks.filter {
        it.title.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add Songs to $playlistName", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search songs...", color = Color.White.copy(alpha = 0.5f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                LazyColumn(
                    modifier = Modifier.heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredTracks) { track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = track.title, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(text = track.artist, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            IconButton(
                                onClick = {
                                    viewModel.addToPlaylist(track, playlistName)
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
}
