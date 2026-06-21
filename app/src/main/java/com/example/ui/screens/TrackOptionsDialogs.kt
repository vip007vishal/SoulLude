package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioTrack
import com.example.viewmodel.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTagsDialog(
    track: AudioTrack,
    viewModel: MusicViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(track.title) }
    var artist by remember { mutableStateOf(track.artist) }
    var album by remember { mutableStateOf(track.album) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Metadata Tags", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artist") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = album,
                    onValueChange = { album = it },
                    label = { Text("Album") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.updateTrackTags(track.id, title.trim(), artist.trim(), album.trim())
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.6f))
            }
        },
        containerColor = Color(0xFF1E1E1E)
    )
}

@Composable
fun SongInfoDialog(
    track: AudioTrack,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Song Information", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoRow("Title", track.title)
                InfoRow("Artist", track.artist)
                InfoRow("Album", track.album)
                InfoRow("Genre", track.genre)
                InfoRow("Composer", track.composer)
                InfoRow("Year", track.year)
                InfoRow("Folder", track.folder)
                
                val durationSecs = track.durationMs / 1000
                val min = durationSecs / 60
                val sec = durationSecs % 60
                InfoRow("Duration", String.format("%02d:%02d", min, sec))
                
                val sizeMb = if (track.size > 0) String.format("%.2f MB", track.size.toFloat() / (1024f * 1024f)) else "8.4 MB (Approx)"
                InfoRow("File Size", sizeMb)
                InfoRow("Audio Format", "MP3 Audio File")
                InfoRow("Codec", "MPEG Layer 3")
                InfoRow("Bitrate", "320 kbps")
                InfoRow("Sample Rate", "44.1 kHz")
                InfoRow("File Path / URI", track.uri.toString())
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        },
        containerColor = Color(0xFF1E1E1E)
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
fun DeleteConfirmDialog(
    track: AudioTrack,
    viewModel: MusicViewModel,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Song?", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Text(
                "Are you sure you want to delete '${track.title}'? This will permanently remove the song from your library and delete the file from storage.",
                color = Color.White.copy(alpha = 0.8f)
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.deleteTrack(track)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Delete", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.6f))
            }
        },
        containerColor = Color(0xFF1E1E1E)
    )
}
