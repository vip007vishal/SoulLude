package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
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
fun StatsScreen(viewModel: MusicViewModel) {
    val allTracks = viewModel.tracks.collectAsState().value
    val recentlyPlayed = viewModel.recentlyPlayed.collectAsState(initial = emptyList()).value
    val mostPlayed = viewModel.mostPlayed.collectAsState(initial = emptyList()).value
    val favorites = viewModel.favorites.collectAsState(initial = emptyList()).value
    val allPlaylists = viewModel.customPlaylists.collectAsState(initial = emptyList()).value

    val totalListeningTimeMs = mostPlayed.sumOf { it.durationMs * 3 } // Mock total time
    val hours = totalListeningTimeMs / 3600000
    val minutes = (totalListeningTimeMs % 3600000) / 60000

    val themeMode by viewModel.themeMode.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = getThemeGradientColors(themeMode)
                )
            )
            .padding(top = 24.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Text(
                "Your Music Insights",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Overview Cards vertically scrollable
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatOverviewCard(title = "Total Songs", value = "${allTracks.size}", icon = Icons.Default.MusicNote, modifier = Modifier.weight(1f))
                StatOverviewCard(title = "Playlists", value = "${allPlaylists.size}", icon = Icons.AutoMirrored.Filled.PlaylistPlay, modifier = Modifier.weight(1f))
                StatOverviewCard(title = "Favorites", value = "${favorites.size}", icon = Icons.Default.Favorite, modifier = Modifier.weight(1f))
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatOverviewCard(title = "Total Time", value = "${hours}h ${minutes}m", icon = Icons.Default.Timer, modifier = Modifier.weight(1f))
                StatOverviewCard(title = "Daily Avg", value = "1h 45m", icon = Icons.Default.AccessTime, modifier = Modifier.weight(1f))
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Most Played Songs
        item {
            SectionTitle("Most Played Songs")
            if (mostPlayed.isEmpty()) {
                Text("No data available", color = Color.White.copy(alpha=0.5f), modifier = Modifier.padding(16.dp))
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(mostPlayed.take(10)) { track ->
                        val playCount = viewModel.getPlayCount(track.id)
                        MostPlayedItem(track = track, plays = playCount, viewModel = viewModel, contextList = mostPlayed)
                    }
                }
            }
        }

        // Recently Played
        item {
            SectionTitle("Recently Played")
            if (recentlyPlayed.isEmpty()) {
                Text("No data available", color = Color.White.copy(alpha=0.5f), modifier = Modifier.padding(16.dp))
            } else {
                recentlyPlayed.take(5).forEach { track ->
                    RecentHistoryItem(track) { viewModel.playTrack(track, recentlyPlayed, contextName = "Recently Played") }
                }
            }
        }
        
        // Favorite Genres
        item {
            SectionTitle("Favorite Genres")
            val mockGenres = listOf(
                Pair("Pop", 0.40f),
                Pair("Rock", 0.25f),
                Pair("Electronic", 0.20f),
                Pair("Classical", 0.15f)
            )
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                mockGenres.forEach { (genre, percentage) ->
                    GenreStatItem(genre = genre, percentage = percentage)
                }
            }
        }

        // Achievements
        item {
            SectionTitle("Milestones & Achievements")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { AchievementCard(title = "100 Favorites", icon = Icons.Default.FavoriteBorder, color = Color(0xFFE91E63)) }
                item { AchievementCard(title = "10k Minutes", icon = Icons.Default.Timer, color = Color(0xFF9C27B0)) }
                item { AchievementCard(title = "Night Owl", icon = Icons.Default.Nightlight, color = Color(0xFF3F51B5)) }
                item { AchievementCard(title = "1 Year Streak", icon = Icons.Default.EmojiEvents, color = Color(0xFFFFC107)) }
            }
        }

        // Monthly Recap Action
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE91E63).copy(alpha = 0.8f))
                    .clickable { }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Loyalty, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Your Monthly Music Recap is Ready!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("See your top songs and artists of the month.", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun StatOverviewCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(title, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}



@Composable
fun AchievementCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
fun MostPlayedItem(track: AudioTrack, plays: Int, viewModel: MusicViewModel, contextList: List<AudioTrack>) {
    Column(
        modifier = Modifier.width(120.dp).clickable { viewModel.playTrack(track, contextList, contextName = "Most Played") },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = track.albumArtUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                error = androidx.compose.ui.graphics.vector.rememberVectorPainter(image = Icons.Default.MusicNote)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(track.title, color = Color.White, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 14.sp)
        Text("$plays Plays", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
    }
}

@Composable
fun RecentHistoryItem(track: AudioTrack, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable { onClick() }
            .padding(12.dp),
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
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                error = androidx.compose.ui.graphics.vector.rememberVectorPainter(image = Icons.Default.MusicNote)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(track.title, color = Color.White, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(track.artist, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text("3 hrs ago", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
    }
}

@Composable
fun GenreStatItem(genre: String, percentage: Float) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(genre, color = Color.White, fontSize = 14.sp)
            Text("${(percentage * 100).toInt()}%", color = Color.White.copy(alpha=0.7f), fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = Color.White.copy(alpha = 0.1f)
        )
    }
}
