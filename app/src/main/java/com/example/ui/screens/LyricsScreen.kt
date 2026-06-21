package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AudioTrack
import com.example.viewmodel.MusicViewModel
import com.example.ui.theme.getThemeGradientColors
import androidx.compose.ui.graphics.Brush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsScreen(viewModel: MusicViewModel, onBack: () -> Unit) {
    val currentTrack = viewModel.currentTrack.collectAsState().value
    var showMenu by remember { mutableStateOf(false) }

    val positionMs by viewModel.currentPositionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()

    val themeMode by viewModel.themeMode.collectAsState()

    val mockLyrics = listOf(
        "I found a love for me",
        "Darling just dive right in",
        "And follow my lead",
        "Well I found a girl beautiful and sweet",
        "I never knew you were the someone waiting for me",
        "'Cause we were just kids when we fell in love",
        "Not knowing what it was",
        "I will not give you up this time",
        "But darling, just kiss me slow",
        "Your heart is all I own",
        "And in your eyes, you're holding mine",
        "Baby, I'm dancing in the dark",
        "With you between my arms",
        "Barefoot on the grass",
        "Listening to our favorite song"
    )

    val segmentDuration = if (durationMs > 0) durationMs / mockLyrics.size.coerceAtLeast(1) else 10000L
    val activeIndex = if (segmentDuration > 0) (positionMs / segmentDuration).toInt().coerceIn(0, mockLyrics.size - 1) else 0

    val listState = rememberLazyListState()

    LaunchedEffect(activeIndex) {
        if (mockLyrics.isNotEmpty()) {
            listState.animateScrollToItem(activeIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = getThemeGradientColors(themeMode)
                )
            )
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.3f))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = currentTrack?.albumArtUri,
                    contentDescription = "Album Art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = androidx.compose.ui.graphics.vector.rememberVectorPainter(image = Icons.Default.MusicNote)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = currentTrack?.title ?: "Unknown Title", color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = currentTrack?.artist ?: "Unknown Artist", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = Color.White)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    DropdownMenuItem(text = { Text("Edit Lyrics", color = Color.White) }, onClick = { showMenu = false })
                    DropdownMenuItem(text = { Text("Import Lyrics File", color = Color.White) }, onClick = { showMenu = false })
                    DropdownMenuItem(text = { Text("Export Lyrics", color = Color.White) }, onClick = { showMenu = false })
                    DropdownMenuItem(text = { Text("Sync Lyrics", color = Color.White) }, onClick = { showMenu = false })
                    DropdownMenuItem(text = { Text("Translate Lyrics", color = Color.White) }, onClick = { showMenu = false })
                    DropdownMenuItem(text = { Text("Copy Lyrics", color = Color.White) }, onClick = { showMenu = false })
                    DropdownMenuItem(text = { Text("Share Lyrics", color = Color.White) }, onClick = { showMenu = false })
                    DropdownMenuItem(text = { Text("Search Lyrics Online", color = Color.White) }, onClick = { showMenu = false })
                    DropdownMenuItem(text = { Text("Fullscreen Mode", color = Color.White) }, onClick = { showMenu = false })
                    DropdownMenuItem(text = { Text("Delete Lyrics", color = Color.Red) }, onClick = { showMenu = false })
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(onClick = {}, label = { Text("Normal") }, colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha=0.2f)))
            AssistChip(onClick = {}, label = { Text("Synchronized") })
            AssistChip(onClick = {}, label = { Text("Karaoke") })
        }

        // Lyrics Body
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            contentPadding = PaddingValues(vertical = 160.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            itemsIndexed(mockLyrics) { index, line ->
                val isCurrent = index == activeIndex
                val isPast = index < activeIndex
                Text(
                    text = line,
                    color = if (isCurrent) Color.White else if (isPast) Color.White.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.25f),
                    fontSize = if (isCurrent) 28.sp else 22.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Start,
                    lineHeight = 36.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.seekTo(index * segmentDuration)
                        }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(16.dp)
                ) {
                    Text("Song Writer: Ed Sheeran", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    Text("Language: English", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }
            }
        }
    }
}
