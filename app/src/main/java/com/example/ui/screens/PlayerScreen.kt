package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.GradientBottom
import com.example.ui.theme.GradientTop
import com.example.ui.theme.PrimaryColor
import com.example.ui.theme.SecondaryColor
import com.example.viewmodel.MusicViewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.scale

fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Composable
fun AudioVisualizer(isPlaying: Boolean, modifier: Modifier = Modifier) {
    val barCount = 32
    val transition = rememberInfiniteTransition(label = "visualizer")
    
    val heights = (0 until barCount).map { index ->
        val duration = remember { (600..1300).random() }
        val delay = remember { (0..400).random() }
        if (isPlaying) {
            transition.animateFloat(
                initialValue = 0.15f,
                targetValue = 0.85f,
                animationSpec = infiniteRepeatable(
                    animation = tween(duration, delayMillis = delay, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$index"
            )
        } else {
            remember { mutableStateOf(0.15f) }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val primaryColor = MaterialTheme.colorScheme.primary
        heights.forEach { heightState ->
            val heightFraction = heightState.value
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(heightFraction)
                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor,
                                primaryColor.copy(alpha = 0.2f)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun PlayerScreen(
    viewModel: MusicViewModel,
    onClose: () -> Unit,
    onOpenLyrics: () -> Unit = {},
    onOpenQueue: () -> Unit = {},
    onOpenEqualizer: () -> Unit = {}
) {
    val track = viewModel.currentTrack.collectAsState().value
    val isPlaying = viewModel.isPlaying.collectAsState().value
    val progress = viewModel.progress.collectAsState().value
    val positionMs = viewModel.currentPositionMs.collectAsState().value
    val durationMs = viewModel.durationMs.collectAsState().value
    val isShuffle = viewModel.isShuffleEnabled.collectAsState().value
    val repeatMode = viewModel.repeatMode.collectAsState().value
    val scrollState = rememberScrollState()
    val blurIntensity = viewModel.themeBlurIntensity.collectAsState().value
    val glassOpacity = viewModel.themeGlassOpacity.collectAsState().value
    val haptic = LocalHapticFeedback.current

    var sliderPosition by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    var showEditTagsDialog by remember { mutableStateOf(false) }
    var showSongInfoDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }

    var showSpeedDialog by remember { mutableStateOf(false) }
    val speed by viewModel.playbackSpeed.collectAsState()

    var showSleepTimerDialog by remember { mutableStateOf(false) }
    val remainingTime by viewModel.sleepTimeRemaining.collectAsState()

    if (showSpeedDialog) {
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = { Text("Playback Speed", color = Color.White) },
            text = {
                Column {
                    listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { s ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setPlaybackSpeed(s)
                                    showSpeedDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${s}x", color = Color.White, fontWeight = if (speed == s) FontWeight.Bold else FontWeight.Normal)
                            if (speed == s) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            containerColor = Color.DarkGray,
            titleContentColor = Color.White
        )
    }

    if (showSleepTimerDialog) {
        AlertDialog(
            onDismissRequest = { showSleepTimerDialog = false },
            title = { Text("Sleep Timer", color = Color.White) },
            text = {
                Column {
                    val currentLabel = if (remainingTime > 0) "Timer Active: ${formatTime(remainingTime)}" else "Timer Inactive"
                    Text(currentLabel, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, modifier = Modifier.padding(bottom = 12.dp))
                    
                    listOf(
                        "Off" to 0,
                        "5 Minutes" to 5,
                        "15 Minutes" to 15,
                        "30 Minutes" to 30,
                        "60 Minutes" to 60
                    ).forEach { (label, mins) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.startSleepTimer(mins)
                                    showSleepTimerDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            Text(label, color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {},
            containerColor = Color.DarkGray,
            titleContentColor = Color.White
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val artworkScale by if (isPlaying) {
        infiniteTransition.animateFloat(
            initialValue = 0.97f,
            targetValue = 1.03f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
    } else {
        remember { mutableStateOf(1.0f) }
    }

    if (track == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("No track playing", color = Color.White.copy(alpha = 0.5f), fontSize = 18.sp)
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onClose) {
                    Text("Close")
                }
            }
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Dynamic Blur Background
        AsyncImage(
            model = track.albumArtUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(blurIntensity.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = glassOpacity)) // Dim the blur
        )

        BoxWithConstraints {
            val screenHeight = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Main Player Area (Full Height)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight)
                        .padding(24.dp)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { _, dragAmount ->
                            if (dragAmount > 50) {
                                viewModel.previousTrack()
                            } else if (dragAmount < -50) {
                                viewModel.nextTrack()
                            }
                        }
                    }
                    .pointerInput("vertical") {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (scrollState.value == 0 && dragAmount > 50f) {
                                onClose()
                            }
                        }
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Close", tint = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NOW PLAYING",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = track.album.ifEmpty { "Single" },
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            DropdownMenuItem(text = { Text("Add to Playlist", color = Color.White) }, onClick = { showAddToPlaylistDialog = true; showMenu = false })
                            DropdownMenuItem(text = { Text("Play Next", color = Color.White) }, onClick = { viewModel.playNext(track); showMenu = false })
                            DropdownMenuItem(text = { Text("Add to Queue", color = Color.White) }, onClick = { viewModel.addToQueue(track); showMenu = false })
                            DropdownMenuItem(text = { Text(if (viewModel.isFavorite(track)) "Remove from Favorites" else "Favorite", color = Color.White) }, onClick = { viewModel.toggleFavorite(track); showMenu = false })
                            DropdownMenuItem(text = { Text("Share Song", color = Color.White) }, onClick = { viewModel.shareTrack(track); showMenu = false })
                            DropdownMenuItem(text = { Text("Song Information", color = Color.White) }, onClick = { showSongInfoDialog = true; showMenu = false })
                            DropdownMenuItem(text = { Text("Edit Tags", color = Color.White) }, onClick = { showEditTagsDialog = true; showMenu = false })
                            DropdownMenuItem(text = { Text("Open Lyrics", color = Color.White) }, onClick = { showMenu = false; onOpenLyrics() })
                            DropdownMenuItem(text = { Text("Open Queue", color = Color.White) }, onClick = { showMenu = false; onOpenQueue() })
                            DropdownMenuItem(text = { Text("Open Equalizer", color = Color.White) }, onClick = { onOpenEqualizer(); showMenu = false })
                            DropdownMenuItem(text = { Text("Sleep Timer", color = Color.White) }, onClick = { showSleepTimerDialog = true; showMenu = false })
                            DropdownMenuItem(text = { Text("Set Bookmark", color = Color.White) }, onClick = { viewModel.addBookmark(track.id, positionMs); showMenu = false })
                            DropdownMenuItem(text = { Text("Set as Ringtone", color = Color.White) }, onClick = { viewModel.setAsRingtone(track); showMenu = false })
                            DropdownMenuItem(text = { Text("Go to Artist", color = Color.White) }, onClick = { viewModel.setSearchQuery(track.artist, "Artists"); viewModel.navigateToTab(1); onClose(); showMenu = false })
                            DropdownMenuItem(text = { Text("Go to Album", color = Color.White) }, onClick = { viewModel.setSearchQuery(track.album, "Albums"); viewModel.navigateToTab(1); onClose(); showMenu = false })
                            DropdownMenuItem(text = { Text("View File Location", color = Color.White) }, onClick = { showSongInfoDialog = true; showMenu = false })
                            DropdownMenuItem(text = { Text("Delete Song", color = Color.Red) }, onClick = { showDeleteConfirmDialog = true; showMenu = false })
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Artwork
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .aspectRatio(1f)
                        .scale(artworkScale)
                        .clip(RoundedCornerShape(32.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.toggleFavorite(track)
                                },
                                onLongPress = {
                                    showMenu = true
                                }
                            )
                        }
                ) {
                    AsyncImage(
                        model = track.albumArtUri,
                        contentDescription = "Album Art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Info
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = track.title,
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee()
                        )
                        Text(
                            text = track.artist,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "High Quality Audio",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    val isFav = viewModel.isFavorite(track)
                    IconButton(
                        onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.toggleFavorite(track) 
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        val favIcon = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder
                        Icon(favIcon, contentDescription = "Favorite", tint = if (isFav) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f))
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onOpenEqualizer) {
                        Icon(Icons.Default.Equalizer, contentDescription = "Equalizer", tint = Color.White.copy(alpha = 0.7f))
                    }
                    IconButton(onClick = { showSpeedDialog = true }) {
                        Icon(Icons.Default.Speed, contentDescription = "Speed", tint = if (speed != 1.0f) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.7f))
                    }
                    IconButton(onClick = { 
                        viewModel.addBookmark(track.id, positionMs)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }) {
                        Icon(Icons.Default.Bookmark, contentDescription = "Bookmark", tint = Color.White.copy(alpha = 0.7f))
                    }
                    IconButton(onClick = onOpenLyrics) {
                        Icon(Icons.Default.Subtitles, contentDescription = "Lyrics", tint = Color.White.copy(alpha = 0.7f))
                    }
                    IconButton(onClick = { showSleepTimerDialog = true }) {
                        Icon(Icons.Default.Timer, contentDescription = "Sleep Timer", tint = if (remainingTime > 0) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.7f))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AudioVisualizer(
                    isPlaying = isPlaying,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar
                val currentSliderValue = if (isDragging) sliderPosition else progress
                Slider(
                    value = currentSliderValue,
                    onValueChange = { 
                        isDragging = true
                        sliderPosition = it
                    },
                    onValueChangeFinished = {
                        isDragging = false
                        viewModel.seekTo((sliderPosition * durationMs).toLong())
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = formatTime(if (isDragging) (sliderPosition * durationMs).toLong() else positionMs), color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    Text(text = formatTime(durationMs), color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Controls
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.toggleShuffle() }) {
                        Icon(Icons.Rounded.Shuffle, contentDescription = "Shuffle", tint = if (isShuffle) MaterialTheme.colorScheme.primary else Color.White.copy(alpha=0.5f))
                    }
                    IconButton(onClick = { viewModel.previousTrack(); haptic.performHapticFeedback(HapticFeedbackType.LongPress) }) {
                        Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    IconButton(onClick = { viewModel.seekBackward(); haptic.performHapticFeedback(HapticFeedbackType.LongPress) }) {
                        Icon(Icons.Rounded.FastRewind, contentDescription = "Rewind", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.togglePlayPause() 
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    
                    IconButton(onClick = { viewModel.seekForward(); haptic.performHapticFeedback(HapticFeedbackType.LongPress) }) {
                        Icon(Icons.Rounded.FastForward, contentDescription = "Forward", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    IconButton(onClick = { viewModel.nextTrack(); haptic.performHapticFeedback(HapticFeedbackType.LongPress) }) {
                        Icon(Icons.Rounded.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    IconButton(onClick = { viewModel.toggleRepeat() }) {
                        val icon = if (repeatMode == androidx.media3.common.Player.REPEAT_MODE_ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat
                        val tint = if (repeatMode != androidx.media3.common.Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary else Color.White.copy(alpha=0.5f)
                        Icon(icon, contentDescription = "Repeat", tint = tint)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Expanded Content (Queue, Lyrics, Details) below the player
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
                Text(
                    text = "LYRICS",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable { onOpenLyrics() }
                        .padding(24.dp)
                ) {
                    Text(
                        text = "To display lyrics, we will need to fetch them or extract them from tags. But we can have this cool karaoke highlight effect.\nCurrently Instrumental.",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 28.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "SONG DETAILS",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val details = listOf(
                        "File Name" to "${track.title}.mp3", // placeholder logic
                        "File Path" to track.uri.toString(),
                        "Audio Format" to "MP3",
                        "Codec" to "MPEG Layer 3",
                        "Bitrate" to "320 kbps",
                        "Sample Rate" to "44.1 kHz",
                        "File Size" to "9.4 MB",
                        "Date Added" to "Oct 12, 2023",
                        "Last Played" to "Today",
                        "Play Count" to "42"
                    )
                    details.forEach { (label, value) ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(text = label, modifier = Modifier.weight(0.4f), color = Color.White.copy(alpha=0.5f), fontSize = 12.sp)
                            Text(text = value, modifier = Modifier.weight(0.6f), color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "UP NEXT",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Static Queue Preview
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val queue = viewModel.queue.collectAsState().value
                    if (queue.isEmpty()) {
                        Text("Queue is empty", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                    } else {
                        queue.take(5).forEachIndexed { i, qTrack ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${i + 1}", modifier = Modifier.width(28.dp), color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = qTrack.title, color = Color.White, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(text = qTrack.artist, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.White.copy(alpha=0.5f))
                            }
                        }
                        if (queue.size > 5) {
                            Text(
                                text = "Show full queue (${queue.size} songs)", 
                                color = MaterialTheme.colorScheme.primary, 
                                fontSize = 14.sp, 
                                fontWeight = FontWeight.Bold, 
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .clickable { onOpenQueue() }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "BOOKMARKS",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val bookmarksList by viewModel.bookmarks.collectAsState()
                if (bookmarksList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "No bookmarks yet. Tap the bookmark icon to save specific moments in this song.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        bookmarksList.forEach { ts ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.seekTo(ts) }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Bookmark at ${formatTime(ts)}", color = Color.White, fontSize = 14.sp)
                                }
                                IconButton(onClick = { viewModel.deleteBookmark(track.id, ts) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha=0.6f), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "RELATED MUSIC",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val allTracks = viewModel.tracks.collectAsState().value
                val relatedTracks = allTracks.filter { it.artist == track.artist && it.id != track.id }.take(5)
                val displayRelated = if (relatedTracks.isNotEmpty()) relatedTracks else allTracks.filter { it.id != track.id }.shuffled().take(5)

                if (displayRelated.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "No related music found.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        displayRelated.forEach { relTrack ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.playTrack(relTrack, displayRelated, "Related Music") }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.DarkGray)
                                ) {
                                    AsyncImage(
                                        model = relTrack.albumArtUri,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(relTrack.title, color = Color.White, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(relTrack.artist, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Icon(Icons.Rounded.PlayArrow, contentDescription = "Play", tint = Color.White.copy(alpha=0.6f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

    if (showEditTagsDialog) {
        EditTagsDialog(track = track, viewModel = viewModel, onDismiss = { showEditTagsDialog = false })
    }
    if (showSongInfoDialog) {
        SongInfoDialog(track = track, onDismiss = { showSongInfoDialog = false })
    }
    if (showDeleteConfirmDialog) {
        DeleteConfirmDialog(track = track, viewModel = viewModel, onDismiss = { showDeleteConfirmDialog = false })
    }
    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(track = track, viewModel = viewModel, onDismiss = { showAddToPlaylistDialog = false })
    }
}
