package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.model.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.media3.session.SessionToken
import androidx.media3.session.MediaController
import com.example.service.PlaybackService
import android.content.ComponentName
import com.google.common.util.concurrent.ListenableFuture

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

data class PlaylistInfo(val name: String, val tracks: List<AudioTrack>)
data class QueueInfo(
    val id: String,
    val name: String,
    val tracks: List<AudioTrack>,
    val currentTrackId: Long?,
    val positionMs: Long
)

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("music_history", Context.MODE_PRIVATE)

    private val _tracks = MutableStateFlow<List<AudioTrack>>(emptyList())
    val tracks: StateFlow<List<AudioTrack>> = _tracks.asStateFlow()

    private val _historyTrigger = MutableStateFlow(0)

    val customPlaylists: StateFlow<List<PlaylistInfo>> = combine(_tracks, _historyTrigger) { tracksList, _ ->
        val names = prefs.getStringSet("playlists_names", emptySet())?.toList() ?: emptyList()
        names.map { name ->
            val currentIds = prefs.getString("playlist_$name", "") ?: ""
            val idsList = if (currentIds.isEmpty()) emptyList() else currentIds.split(",")
            val playlistTracks = idsList.mapNotNull { id -> tracksList.find { it.id.toString() == id } }
            PlaylistInfo(name = name, tracks = playlistTracks)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val recentlyPlayed: StateFlow<List<AudioTrack>> = combine(_tracks, _historyTrigger) { tracksList, _ ->
        tracksList.filter { prefs.getLong("last_played_${it.id}", 0L) > 0 }
            .sortedByDescending { prefs.getLong("last_played_${it.id}", 0L) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val mostPlayed: StateFlow<List<AudioTrack>> = combine(_tracks, _historyTrigger) { tracksList, _ ->
        tracksList.filter { prefs.getInt("count_${it.id}", 0) > 0 }
            .sortedByDescending { prefs.getInt("count_${it.id}", 0) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val favorites: StateFlow<List<AudioTrack>> = combine(_tracks, _historyTrigger) { tracksList, _ ->
        tracksList.filter { prefs.getBoolean("fav_${it.id}", false) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _currentTrack = MutableStateFlow<AudioTrack?>(null)
    val currentTrack: StateFlow<AudioTrack?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()
    
    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()
    
    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()
    
    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _queue = MutableStateFlow<List<AudioTrack>>(emptyList())
    val queue: StateFlow<List<AudioTrack>> = _queue.asStateFlow()

    private val _queues = MutableStateFlow<List<QueueInfo>>(emptyList())
    val queues: StateFlow<List<QueueInfo>> = _queues.asStateFlow()

    private val _activeQueueId = MutableStateFlow<String>("")
    val activeQueueId: StateFlow<String> = _activeQueueId.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var player: Player? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var isSwitchingQueue = false

    private val themePrefs = application.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    private val _themePrimaryColor = MutableStateFlow(themePrefs.getInt("primary_color", 0xFFD0BCFF.toInt()))
    val themePrimaryColor: StateFlow<Int> = _themePrimaryColor.asStateFlow()

    private val _themeBlurIntensity = MutableStateFlow(themePrefs.getFloat("blur_intensity", 100f))
    val themeBlurIntensity: StateFlow<Float> = _themeBlurIntensity.asStateFlow()

    private val _themeGlassOpacity = MutableStateFlow(themePrefs.getFloat("glass_opacity", 0.5f))
    val themeGlassOpacity: StateFlow<Float> = _themeGlassOpacity.asStateFlow()

    private val _seekDurationSeconds = MutableStateFlow(5)
    val seekDurationSeconds: StateFlow<Int> = _seekDurationSeconds.asStateFlow()

    private val _themeMode = MutableStateFlow(themePrefs.getString("theme_mode", "Dark") ?: "Dark")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private var sleepTimerJob: kotlinx.coroutines.Job? = null
    private val _sleepTimeRemaining = MutableStateFlow(0L)
    val sleepTimeRemaining: StateFlow<Long> = _sleepTimeRemaining.asStateFlow()

    private val _eqEnabled = MutableStateFlow(false)
    val eqEnabled: StateFlow<Boolean> = _eqEnabled.asStateFlow()

    private val _eqBands = MutableStateFlow(listOf(0f, 0f, 0f, 0f, 0f))
    val eqBands: StateFlow<List<Float>> = _eqBands.asStateFlow()

    private val _bassBoost = MutableStateFlow(0f)
    val bassBoost: StateFlow<Float> = _bassBoost.asStateFlow()

    private val _surround = MutableStateFlow(0f)
    val surround: StateFlow<Float> = _surround.asStateFlow()

    private val _selectedPreset = MutableStateFlow("Flat")
    val selectedPreset: StateFlow<String> = _selectedPreset.asStateFlow()

    private val _bookmarks = MutableStateFlow<List<Long>>(emptyList())
    val bookmarks: StateFlow<List<Long>> = _bookmarks.asStateFlow()

    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    fun updatePrimaryColor(color: Int) {
        themePrefs.edit().putInt("primary_color", color).apply()
        _themePrimaryColor.value = color
    }

    fun updateBlurIntensity(intensity: Float) {
        themePrefs.edit().putFloat("blur_intensity", intensity).apply()
        _themeBlurIntensity.value = intensity
    }

    fun updateGlassOpacity(opacity: Float) {
        themePrefs.edit().putFloat("glass_opacity", opacity).apply()
        _themeGlassOpacity.value = opacity
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        themePrefs.edit().putString("theme_mode", mode).apply()
        val color = when (mode) {
            "AMOLED Black" -> 0xFFFFFFFF.toInt()
            "Warm Orange" -> 0xFFFF8F00.toInt()
            "Forest Green" -> 0xFF2E7D32.toInt()
            "Deep Blue" -> 0xFF1565C0.toInt()
            else -> 0xFFD0BCFF.toInt() // Default Purple / Dark
        }
        updatePrimaryColor(color)
    }

    init {
        _seekDurationSeconds.value = prefs.getInt("seek_duration", 5)
        _eqEnabled.value = prefs.getBoolean("eq_enabled", false)
        val eqBandsStr = prefs.getString("eq_bands", "0,0,0,0,0") ?: "0,0,0,0,0"
        _eqBands.value = eqBandsStr.split(",").mapNotNull { it.toFloatOrNull() }.takeIf { it.size == 5 } ?: listOf(0f, 0f, 0f, 0f, 0f)
        _bassBoost.value = prefs.getFloat("bass_boost", 0f)
        _surround.value = prefs.getFloat("surround", 0f)
        _selectedPreset.value = prefs.getString("eq_preset", "Flat") ?: "Flat"
        loadRecentSearches()
        initializePlayer()
        loadLocalMusic()
    }

    private fun initializePlayer() {
        val sessionToken = SessionToken(getApplication(), ComponentName(getApplication(), PlaybackService::class.java))
        controllerFuture = MediaController.Builder(getApplication(), sessionToken).buildAsync()
        controllerFuture?.addListener({
            player = controllerFuture?.get()
            player?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)
                    // Match playing item id back to our track list if possible
                    mediaItem?.mediaId?.toLongOrNull()?.let { mediaId ->
                        val track = _tracks.value.find { it.id == mediaId }
                        if (track != null) {
                            _currentTrack.value = track
                            recordRecentlyPlayed(track)
                            currentTrackIdForIncrement = track.id
                            currentTrackIncremented = false
                            saveActiveQueueState()
                            loadBookmarksForTrack(track.id)
                        }
                    }
                }
                override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
                    super.onTimelineChanged(timeline, reason)
                    updateQueueFromPlayer()
                }
            })
        }, androidx.core.content.ContextCompat.getMainExecutor(getApplication()))
        
        // Progress updater coroutine
        viewModelScope.launch(Dispatchers.Main) {
            while (true) {
                if (_isPlaying.value && player != null) {
                    val duration = player!!.duration.coerceAtLeast(1)
                    val currentPos = player!!.currentPosition
                    val progressFraction = currentPos.toFloat() / duration.toFloat()
                    
                    _progress.value = progressFraction
                    _currentPositionMs.value = currentPos
                    _durationMs.value = duration
                    
                    // Track play count if play duration is above 50%
                    val currentId = currentTrackIdForIncrement
                    if (currentId != null && progressFraction >= 0.5f && !currentTrackIncremented) {
                        val track = _tracks.value.find { it.id == currentId }
                        if (track != null) {
                            incrementPlayCount(track)
                            currentTrackIncremented = true
                        }
                    }
                }
                kotlinx.coroutines.delay(500)
            }
        }
    }

    fun loadLocalMusic() {
        viewModelScope.launch {
            _isLoading.value = true
            val musicList = withContext(Dispatchers.IO) {
                queryMusic(getApplication())
            }
            _tracks.value = musicList
            initializeQueues(musicList)
            _isLoading.value = false
        }
    }

    private fun queryMusic(context: android.content.Context): List<AudioTrack> {
        val tracks = mutableListOf<AudioTrack>()
        val collection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            val query: Cursor? = context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )

            query?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                val artworkUri = Uri.parse("content://media/external/audio/albumart")

                val deletedSet = prefs.getStringSet("deleted_tracks", emptySet()) ?: emptySet()
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    if (deletedSet.contains(id.toString())) continue

                    val title = prefs.getString("override_title_$id", null) ?: cursor.getString(titleColumn) ?: "Unknown Title"
                    val artist = prefs.getString("override_artist_$id", null) ?: cursor.getString(artistColumn) ?: "Unknown Artist"
                    val album = prefs.getString("override_album_$id", null) ?: cursor.getString(albumColumn) ?: "Unknown Album"
                    val albumId = cursor.getLong(albumIdColumn)
                    val duration = cursor.getLong(durationColumn)

                    val contentUri: Uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val albumArtUri = ContentUris.withAppendedId(artworkUri, albumId)

                    tracks.add(
                        AudioTrack(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            albumId = albumId,
                            durationMs = duration,
                            uri = contentUri,
                            albumArtUri = albumArtUri
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (tracks.isEmpty()) {
            // Mocks for emulator usage when local library is empty
            val deletedSet = prefs.getStringSet("deleted_tracks", emptySet()) ?: emptySet()
            for (i in 1..20) {
                val mockId = i.toLong()
                if (deletedSet.contains(mockId.toString())) continue

                val title = prefs.getString("override_title_$mockId", null) ?: "Sample Track $i"
                val artist = prefs.getString("override_artist_$mockId", null) ?: (if (i % 2 == 0) "Artist A" else "Artist B")
                val album = prefs.getString("override_album_$mockId", null) ?: "Sample Album"

                tracks.add(
                    AudioTrack(
                        id = mockId,
                        title = title,
                        artist = artist,
                        album = album,
                        albumId = 1L,
                        durationMs = 180000L + (i * 10000L),
                        uri = Uri.parse(""), // Empty intentionally to not crash but show UI
                        albumArtUri = Uri.parse("")
                    )
                )
            }
        }

        return tracks
    }

    private var currentTrackIncremented = false
    private var currentTrackIdForIncrement: Long? = null

    private fun recordRecentlyPlayed(track: AudioTrack) {
        prefs.edit()
            .putLong("last_played_${track.id}", System.currentTimeMillis())
            .apply()
        _historyTrigger.value = _historyTrigger.value + 1
    }

    fun getPlayCount(trackId: Long): Int {
        return prefs.getInt("count_$trackId", 0)
    }

    private fun incrementPlayCount(track: AudioTrack) {
        val count = prefs.getInt("count_${track.id}", 0)
        prefs.edit()
            .putInt("count_${track.id}", count + 1)
            .apply()
        _historyTrigger.value = _historyTrigger.value + 1
    }

    fun toggleFavorite(track: AudioTrack) {
        val isFav = prefs.getBoolean("fav_${track.id}", false)
        prefs.edit().putBoolean("fav_${track.id}", !isFav).apply()
        _historyTrigger.value = _historyTrigger.value + 1
    }

    fun isFavorite(track: AudioTrack): Boolean {
        return prefs.getBoolean("fav_${track.id}", false)
    }

    private fun initializeQueues(musicList: List<AudioTrack>) {
        if (musicList.isEmpty()) return
        val currentQueueIds = prefs.getStringSet("queue_ids", null)
        
        if (currentQueueIds == null || currentQueueIds.isEmpty()) {
            val defaultIds = setOf("queue_default")
            prefs.edit().apply {
                putStringSet("queue_ids", defaultIds)
                putString("queue_name_queue_default", "Current Queue")
                putString("queue_tracks_queue_default", "")
                putLong("queue_current_track_queue_default", -1L)
                putLong("queue_position_queue_default", 0L)
                
                putString("active_queue_id", "queue_default")
                apply()
            }
        }
        
        val loadedQueues = mutableListOf<QueueInfo>()
        val ids = prefs.getStringSet("queue_ids", emptySet()) ?: emptySet()
        for (id in ids) {
            val name = prefs.getString("queue_name_$id", "Queue") ?: "Queue"
            val tracksStr = prefs.getString("queue_tracks_$id", "") ?: ""
            val trackIds = if (tracksStr.isEmpty()) emptyList() else tracksStr.split(",").mapNotNull { it.toLongOrNull() }
            val qTracks = trackIds.mapNotNull { tId -> musicList.find { it.id == tId } }
            val currentTrackIdVal = prefs.getLong("queue_current_track_$id", -1L)
            val currentTrackId = if (currentTrackIdVal != -1L) currentTrackIdVal else null
            val pos = prefs.getLong("queue_position_$id", 0L)
            
            loadedQueues.add(
                QueueInfo(
                    id = id,
                    name = name,
                    tracks = qTracks,
                    currentTrackId = currentTrackId,
                    positionMs = pos
                )
            )
        }
        
        _queues.value = loadedQueues
        
        val activeId = prefs.getString("active_queue_id", "queue_default") ?: "queue_default"
        val finalActiveId = if (ids.contains(activeId)) activeId else (ids.firstOrNull() ?: "queue_default")
        _activeQueueId.value = finalActiveId
        
        val activeQ = loadedQueues.find { it.id == finalActiveId }
        _queue.value = activeQ?.tracks ?: emptyList()
        
        viewModelScope.launch(Dispatchers.Main) {
            switchQueue(finalActiveId, playWhenReady = false)
        }
    }

    fun saveActiveQueueState() {
        val p = player ?: return
        val activeId = _activeQueueId.value
        if (activeId.isEmpty() || isSwitchingQueue) return
        
        val currentTrackId = _currentTrack.value?.id ?: -1L
        val currentPosition = p.currentPosition
        val currentTracks = _queue.value
        val trackIdsStr = currentTracks.map { it.id }.joinToString(",")
        
        prefs.edit().apply {
            putString("queue_tracks_$activeId", trackIdsStr)
            putLong("queue_current_track_$activeId", currentTrackId)
            putLong("queue_position_$activeId", currentPosition)
            apply()
        }
        
        _queues.value = _queues.value.map {
            if (it.id == activeId) {
                it.copy(
                    tracks = currentTracks,
                    currentTrackId = if (currentTrackId != -1L) currentTrackId else null,
                    positionMs = currentPosition
                )
            } else {
                it
            }
        }
    }

    fun switchQueue(id: String, playWhenReady: Boolean = true) {
        val p = player ?: return
        val previousActiveId = _activeQueueId.value
        if (previousActiveId.isNotEmpty() && previousActiveId != id) {
            saveActiveQueueState()
        }
        
        val targetQueue = _queues.value.find { it.id == id } ?: return
        isSwitchingQueue = true
        
        _activeQueueId.value = id
        prefs.edit().putString("active_queue_id", id).apply()
        
        _queue.value = targetQueue.tracks
        
        val mediaItems = targetQueue.tracks.map { t ->
            MediaItem.Builder()
                .setMediaId(t.id.toString())
                .setUri(t.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(t.title)
                        .setArtist(t.artist)
                        .setAlbumTitle(t.album)
                        .setArtworkUri(t.albumArtUri)
                        .build()
                )
                .build()
        }
        
        p.setMediaItems(mediaItems)
        
        val targetTrackId = targetQueue.currentTrackId
        val targetIndex = if (targetTrackId != null) {
            targetQueue.tracks.indexOfFirst { it.id == targetTrackId }.takeIf { it >= 0 } ?: 0
        } else {
            0
        }
        
        if (targetQueue.tracks.isNotEmpty()) {
            p.seekTo(targetIndex, targetQueue.positionMs)
            p.prepare()
            if (playWhenReady) {
                p.play()
            }
            _currentTrack.value = targetQueue.tracks.getOrNull(targetIndex)
        } else {
            p.clearMediaItems()
            _currentTrack.value = null
        }
        
        isSwitchingQueue = false
    }

    fun playCollection(queueName: String, tracks: List<AudioTrack>, startIndex: Int = 0) {
        val queueId = "col_" + queueName.hashCode().toString()
        val existingQueue = _queues.value.find { it.id == queueId }
        
        if (existingQueue == null) {
            val newQueue = QueueInfo(
                id = queueId,
                name = queueName,
                tracks = tracks,
                currentTrackId = tracks.getOrNull(startIndex)?.id,
                positionMs = 0L
            )
            
            val currentQueueIds = prefs.getStringSet("queue_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
            currentQueueIds.add(queueId)
            
            prefs.edit().apply {
                putStringSet("queue_ids", currentQueueIds)
                putString("queue_name_$queueId", queueName)
                putString("queue_tracks_$queueId", tracks.map { it.id }.joinToString(","))
                putLong("queue_current_track_$queueId", tracks.getOrNull(startIndex)?.id ?: -1L)
                putLong("queue_position_$queueId", 0L)
                apply()
            }
            
            _queues.value = _queues.value + newQueue
        } else {
            val updatedQueue = existingQueue.copy(
                tracks = tracks,
                currentTrackId = tracks.getOrNull(startIndex)?.id ?: existingQueue.currentTrackId,
                positionMs = if (startIndex != 0) 0L else existingQueue.positionMs
            )
            _queues.value = _queues.value.map { if (it.id == queueId) updatedQueue else it }
            
            prefs.edit().apply {
                putString("queue_tracks_$queueId", tracks.map { it.id }.joinToString(","))
                putLong("queue_current_track_$queueId", updatedQueue.currentTrackId ?: -1L)
                putLong("queue_position_$queueId", updatedQueue.positionMs)
                apply()
            }
        }
        
        switchQueue(queueId, playWhenReady = true)
        
        if (startIndex >= 0 && startIndex < tracks.size) {
            player?.seekTo(startIndex, 0L)
            _currentTrack.value = tracks[startIndex]
        }
    }

    fun playPlaylist(playlist: List<AudioTrack>, startIndex: Int = 0, contextName: String? = null) {
        if (playlist.isEmpty()) return

        if (contextName != null) {
            playCollection(contextName, playlist, startIndex)
            return
        }

        player?.let { p ->
            isSwitchingQueue = true
            val mediaItems = playlist.map { t ->
                MediaItem.Builder()
                    .setMediaId(t.id.toString())
                    .setUri(t.uri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(t.title)
                            .setArtist(t.artist)
                            .setAlbumTitle(t.album)
                            .setArtworkUri(t.albumArtUri)
                            .build()
                    )
                    .build()
            }
            p.setMediaItems(mediaItems, startIndex, 0)
            p.prepare()
            p.play()
            
            val track = playlist[startIndex]
            _currentTrack.value = track
            recordRecentlyPlayed(track)
            
            isSwitchingQueue = false
            updateQueueFromPlayer()
        }
    }

    fun playTrack(track: AudioTrack, contextList: List<AudioTrack>? = null, contextName: String? = null) {
        val allTracks = contextList ?: _tracks.value
        val startIndex = allTracks.indexOf(track).takeIf { it >= 0 } ?: 0
        playPlaylist(allTracks, startIndex, contextName)
    }

    fun addToQueue(track: AudioTrack) {
        player?.let { p ->
            val mediaItem = MediaItem.Builder()
                .setMediaId(track.id.toString())
                .setUri(track.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.artist)
                        .setAlbumTitle(track.album)
                        .setArtworkUri(track.albumArtUri)
                        .build()
                )
                .build()
            p.addMediaItem(mediaItem)
            updateQueueFromPlayer()
        }
    }

    fun addAllToQueue(tracks: List<AudioTrack>) {
        if (tracks.isEmpty()) return
        val mediaItems = tracks.map {
            MediaItem.Builder()
                .setMediaId(it.id.toString())
                .setUri(it.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(it.title)
                        .setArtist(it.artist)
                        .setAlbumTitle(it.album)
                        .setArtworkUri(it.albumArtUri)
                        .build()
                )
                .build()
        }
        player?.addMediaItems(mediaItems)
        updateQueueFromPlayer()
    }

    fun playNext(track: AudioTrack) {
        val mediaItem = MediaItem.Builder()
            .setMediaId(track.id.toString())
            .setUri(track.uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(track.artist)
                    .setAlbumTitle(track.album)
                    .setArtworkUri(track.albumArtUri)
                    .build()
            )
            .build()
        val currentIndex = player?.currentMediaItemIndex ?: 0
        player?.addMediaItem(currentIndex + 1, mediaItem)
        updateQueueFromPlayer()
    }

    fun playNext(tracks: List<AudioTrack>) {
        if (tracks.isEmpty()) return
        val mediaItems = tracks.map {
            MediaItem.Builder()
                .setMediaId(it.id.toString())
                .setUri(it.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(it.title)
                        .setArtist(it.artist)
                        .setAlbumTitle(it.album)
                        .setArtworkUri(it.albumArtUri)
                        .build()
                )
                .build()
        }
        val currentIndex = player?.currentMediaItemIndex ?: 0
        player?.addMediaItems(currentIndex + 1, mediaItems)
        updateQueueFromPlayer()
    }

    fun createQueue(name: String, tracks: List<AudioTrack> = emptyList()): String {
        val queueId = "user_" + System.currentTimeMillis() + "_" + name.hashCode()
        val newQueue = QueueInfo(
            id = queueId,
            name = name,
            tracks = tracks,
            currentTrackId = tracks.firstOrNull()?.id,
            positionMs = 0L
        )
        
        val currentQueueIds = prefs.getStringSet("queue_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
        currentQueueIds.add(queueId)
        
        prefs.edit().apply {
            putStringSet("queue_ids", currentQueueIds)
            putString("queue_name_$queueId", name)
            putString("queue_tracks_$queueId", tracks.map { it.id }.joinToString(","))
            putLong("queue_current_track_$queueId", tracks.firstOrNull()?.id ?: -1L)
            putLong("queue_position_$queueId", 0L)
            apply()
        }
        
        _queues.value = _queues.value + newQueue
        _historyTrigger.value = _historyTrigger.value + 1
        return queueId
    }

    fun renameQueue(queueId: String, newName: String) {
        prefs.edit().putString("queue_name_$queueId", newName).apply()
        _queues.value = _queues.value.map {
            if (it.id == queueId) it.copy(name = newName) else it
        }
        _historyTrigger.value = _historyTrigger.value + 1
    }

    fun duplicateQueue(queueId: String, newName: String): String? {
        val source = _queues.value.find { it.id == queueId } ?: return null
        val newQueueId = createQueue(newName, source.tracks)
        
        prefs.edit().apply {
            putLong("queue_current_track_$newQueueId", source.currentTrackId ?: -1L)
            putLong("queue_position_$newQueueId", source.positionMs)
            apply()
        }
        
        _queues.value = _queues.value.map {
            if (it.id == newQueueId) {
                it.copy(
                    currentTrackId = source.currentTrackId,
                    positionMs = source.positionMs
                )
            } else {
                it
            }
        }
        _historyTrigger.value = _historyTrigger.value + 1
        return newQueueId
    }

    fun deleteQueue(queueId: String) {
        val currentQueueIds = prefs.getStringSet("queue_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
        if (currentQueueIds.contains(queueId)) {
            currentQueueIds.remove(queueId)
            prefs.edit().apply {
                putStringSet("queue_ids", currentQueueIds)
                remove("queue_name_$queueId")
                remove("queue_tracks_$queueId")
                remove("queue_current_track_$queueId")
                remove("queue_position_$queueId")
                apply()
            }
            _queues.value = _queues.value.filter { it.id != queueId }
            
            if (_activeQueueId.value == queueId) {
                val nextActive = _queues.value.firstOrNull()?.id ?: "queue_default"
                switchQueue(nextActive, playWhenReady = false)
            }
            _historyTrigger.value = _historyTrigger.value + 1
        }
    }

    fun clearQueue(queueId: String) {
        prefs.edit().apply {
            putString("queue_tracks_$queueId", "")
            putLong("queue_current_track_$queueId", -1L)
            putLong("queue_position_$queueId", 0L)
            apply()
        }
        
        _queues.value = _queues.value.map {
            if (it.id == queueId) {
                it.copy(tracks = emptyList(), currentTrackId = null, positionMs = 0L)
            } else {
                it
            }
        }
        
        if (_activeQueueId.value == queueId) {
            player?.clearMediaItems()
            _currentTrack.value = null
            _queue.value = emptyList()
        }
        _historyTrigger.value = _historyTrigger.value + 1
    }

    fun exportQueue(queueId: String): String {
        val q = _queues.value.find { it.id == queueId } ?: return ""
        return "${q.name}|${q.tracks.map { it.id }.joinToString(",")}"
    }

    fun restoreQueue(data: String): String? {
        val parts = data.split("|")
        if (parts.size < 2) return null
        val name = parts[0]
        val trackIds = parts[1].split(",").mapNotNull { it.toLongOrNull() }
        val tracksList = _tracks.value
        val tracks = trackIds.mapNotNull { id -> tracksList.find { it.id == id } }
        return createQueue(name, tracks)
    }

    fun convertQueueToPlaylist(queueId: String, playlistName: String) {
        val q = _queues.value.find { it.id == queueId } ?: return
        createPlaylist(playlistName)
        q.tracks.forEach { track ->
            addToPlaylist(track, playlistName)
        }
    }

    fun getPlaylists(): List<String> {
        return prefs.getStringSet("playlists_names", emptySet())?.toList() ?: emptyList()
    }

    fun createPlaylist(name: String) {
        val currentPlaylists = getPlaylists().toMutableSet()
        currentPlaylists.add(name)
        prefs.edit().putStringSet("playlists_names", currentPlaylists).apply()
        _historyTrigger.value = _historyTrigger.value + 1
    }

    fun deletePlaylist(name: String) {
        val currentPlaylists = getPlaylists().toMutableSet()
        currentPlaylists.remove(name)
        prefs.edit().putStringSet("playlists_names", currentPlaylists).remove("playlist_$name").apply()
        _historyTrigger.value = _historyTrigger.value + 1
    }

    fun duplicatePlaylist(name: String, newName: String) {
        val currentPlaylists = getPlaylists().toMutableSet()
        if (currentPlaylists.contains(name)) {
            currentPlaylists.add(newName)
            val ids = prefs.getString("playlist_$name", "")
            prefs.edit()
                .putStringSet("playlists_names", currentPlaylists)
                .putString("playlist_$newName", ids)
                .apply()
            _historyTrigger.value = _historyTrigger.value + 1
        }
    }

    fun renamePlaylist(oldName: String, newName: String) {
        val currentPlaylists = getPlaylists().toMutableSet()
        if (currentPlaylists.contains(oldName)) {
            currentPlaylists.remove(oldName)
            currentPlaylists.add(newName)
            val ids = prefs.getString("playlist_$oldName", "")
            prefs.edit()
                .putStringSet("playlists_names", currentPlaylists)
                .remove("playlist_$oldName")
                .putString("playlist_$newName", ids)
                .apply()
            _historyTrigger.value = _historyTrigger.value + 1
        }
    }

    fun addToPlaylist(track: AudioTrack, playlistName: String) {
        val currentIds = prefs.getString("playlist_$playlistName", "") ?: ""
        val idsList = if (currentIds.isEmpty()) mutableListOf() else currentIds.split(",").toMutableList()
        if (!idsList.contains(track.id.toString())) {
            idsList.add(track.id.toString())
            prefs.edit().putString("playlist_$playlistName", idsList.joinToString(",")).apply()
            _historyTrigger.value = _historyTrigger.value + 1
        }
    }

    fun removeFromPlaylist(track: AudioTrack, playlistName: String) {
        val currentIds = prefs.getString("playlist_$playlistName", "") ?: ""
        val idsList = if (currentIds.isEmpty()) mutableListOf() else currentIds.split(",").toMutableList()
        if (idsList.contains(track.id.toString())) {
            idsList.remove(track.id.toString())
            prefs.edit().putString("playlist_$playlistName", idsList.joinToString(",")).apply()
            _historyTrigger.value = _historyTrigger.value + 1
        }
    }

    fun shuffleQueue(queueId: String) {
        val q = _queues.value.find { it.id == queueId } ?: return
        val shuffledTracks = q.tracks.shuffled()
        
        prefs.edit().putString("queue_tracks_$queueId", shuffledTracks.map { it.id }.joinToString(",")).apply()
        
        _queues.value = _queues.value.map {
            if (it.id == queueId) it.copy(tracks = shuffledTracks) else it
        }
        
        if (_activeQueueId.value == queueId) {
            val p = player ?: return
            val currentTrack = _currentTrack.value
            val mediaItems = shuffledTracks.map { t ->
                MediaItem.Builder()
                    .setMediaId(t.id.toString())
                    .setUri(t.uri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(t.title)
                            .setArtist(t.artist)
                            .setAlbumTitle(t.album)
                            .setArtworkUri(t.albumArtUri)
                            .build()
                    )
                    .build()
            }
            p.setMediaItems(mediaItems)
            val newIndex = if (currentTrack != null) shuffledTracks.indexOf(currentTrack).coerceAtLeast(0) else 0
            p.seekTo(newIndex, p.currentPosition)
            p.prepare()
            _queue.value = shuffledTracks
        }
        _historyTrigger.value = _historyTrigger.value + 1
    }

    fun sortQueue(queueId: String) {
        val q = _queues.value.find { it.id == queueId } ?: return
        val sortedTracks = q.tracks.sortedBy { it.title }
        
        prefs.edit().putString("queue_tracks_$queueId", sortedTracks.map { it.id }.joinToString(",")).apply()
        
        _queues.value = _queues.value.map {
            if (it.id == queueId) it.copy(tracks = sortedTracks) else it
        }
        
        if (_activeQueueId.value == queueId) {
            val p = player ?: return
            val currentTrack = _currentTrack.value
            val mediaItems = sortedTracks.map { t ->
                MediaItem.Builder()
                    .setMediaId(t.id.toString())
                    .setUri(t.uri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(t.title)
                            .setArtist(t.artist)
                            .setAlbumTitle(t.album)
                            .setArtworkUri(t.albumArtUri)
                            .build()
                    )
                    .build()
            }
            p.setMediaItems(mediaItems)
            val newIndex = if (currentTrack != null) sortedTracks.indexOf(currentTrack).coerceAtLeast(0) else 0
            p.seekTo(newIndex, p.currentPosition)
            p.prepare()
            _queue.value = sortedTracks
        }
        _historyTrigger.value = _historyTrigger.value + 1
    }

    fun playQueueTrack(track: AudioTrack, index: Int) {
        player?.let { p ->
            if (p.mediaItemCount > index && p.getMediaItemAt(index).mediaId == track.id.toString()) {
                p.seekTo(index, 0L)
                p.play()
            }
        }
    }

    fun removeQueueTrack(queueId: String, index: Int) {
        val q = _queues.value.find { it.id == queueId } ?: return
        if (index in q.tracks.indices) {
            val updatedTracks = q.tracks.toMutableList()
            updatedTracks.removeAt(index)
            
            prefs.edit().putString("queue_tracks_$queueId", updatedTracks.map { it.id }.joinToString(",")).apply()
            
            _queues.value = _queues.value.map {
                if (it.id == queueId) it.copy(tracks = updatedTracks) else it
            }
            
            if (_activeQueueId.value == queueId) {
                player?.removeMediaItem(index)
                _queue.value = updatedTracks
            }
            _historyTrigger.value = _historyTrigger.value + 1
        }
    }

    fun moveQueueTrackToTop(queueId: String, index: Int) {
        val q = _queues.value.find { it.id == queueId } ?: return
        if (index in q.tracks.indices && index > 0) {
            val updatedTracks = q.tracks.toMutableList()
            val item = updatedTracks.removeAt(index)
            updatedTracks.add(0, item)
            
            prefs.edit().putString("queue_tracks_$queueId", updatedTracks.map { it.id }.joinToString(",")).apply()
            
            _queues.value = _queues.value.map {
                if (it.id == queueId) it.copy(tracks = updatedTracks) else it
            }
            
            if (_activeQueueId.value == queueId) {
                player?.let { p ->
                    isSwitchingQueue = true
                    p.moveMediaItem(index, 0)
                    isSwitchingQueue = false
                    updateQueueFromPlayer()
                }
            }
            _historyTrigger.value = _historyTrigger.value + 1
        }
    }

    fun moveQueueTrackToBottom(queueId: String, index: Int) {
        val q = _queues.value.find { it.id == queueId } ?: return
        if (index in q.tracks.indices && index < q.tracks.size - 1) {
            val updatedTracks = q.tracks.toMutableList()
            val item = updatedTracks.removeAt(index)
            updatedTracks.add(item)
            
            prefs.edit().putString("queue_tracks_$queueId", updatedTracks.map { it.id }.joinToString(",")).apply()
            
            _queues.value = _queues.value.map {
                if (it.id == queueId) it.copy(tracks = updatedTracks) else it
            }
            
            if (_activeQueueId.value == queueId) {
                player?.let { p ->
                    isSwitchingQueue = true
                    p.moveMediaItem(index, q.tracks.size - 1)
                    isSwitchingQueue = false
                    updateQueueFromPlayer()
                }
            }
            _historyTrigger.value = _historyTrigger.value + 1
        }
    }

    private fun updateQueueFromPlayer() {
        val p = player ?: return
        if (isSwitchingQueue) return
        val count = p.mediaItemCount
        val tracksList = _tracks.value
        val currentQueue = mutableListOf<AudioTrack>()
        for (i in 0 until count) {
            val item = p.getMediaItemAt(i)
            val mediaId = item.mediaId
            mediaId.toLongOrNull()?.let { id ->
                val track = tracksList.find { it.id == id } ?: run {
                    val meta = item.mediaMetadata
                    AudioTrack(
                        id = id,
                        title = meta.title?.toString() ?: "Unknown Title",
                        artist = meta.artist?.toString() ?: "Unknown Artist",
                        album = meta.albumTitle?.toString() ?: "Unknown Album",
                        albumId = 0L,
                        durationMs = 0L,
                        uri = item.localConfiguration?.uri ?: Uri.EMPTY,
                        albumArtUri = meta.artworkUri ?: Uri.EMPTY
                    )
                }
                currentQueue.add(track)
            }
        }
        _queue.value = currentQueue
        
        val activeId = _activeQueueId.value
        if (activeId.isNotEmpty()) {
            val trackIdsStr = currentQueue.map { it.id }.joinToString(",")
            prefs.edit().putString("queue_tracks_$activeId", trackIdsStr).apply()
            
            _queues.value = _queues.value.map {
                if (it.id == activeId) {
                    it.copy(tracks = currentQueue)
                } else {
                    it
                }
            }
        }
    }


    fun togglePlayPause() {
        player?.let { p ->
            if (p.isPlaying) {
                p.pause()
            } else {
                p.play()
            }
        }
    }
    
    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
        _currentPositionMs.value = positionMs
        player?.duration?.let {
            if (it > 0) _progress.value = positionMs.toFloat() / it.toFloat()
        }
    }

    fun setSeekDuration(seconds: Int) {
        _seekDurationSeconds.value = seconds
        prefs.edit().putInt("seek_duration", seconds).apply()
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        player?.let { p ->
            val param = androidx.media3.common.PlaybackParameters(speed)
            p.playbackParameters = param
        }
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _sleepTimeRemaining.value = 0L
            return
        }
        _sleepTimeRemaining.value = minutes * 60000L
        sleepTimerJob = viewModelScope.launch {
            while (_sleepTimeRemaining.value > 0) {
                kotlinx.coroutines.delay(1000)
                _sleepTimeRemaining.value = (_sleepTimeRemaining.value - 1000).coerceAtLeast(0)
                if (_sleepTimeRemaining.value == 0L) {
                    player?.pause()
                    _isPlaying.value = false
                }
            }
        }
    }

    fun stopSleepTimer() {
        sleepTimerJob?.cancel()
        _sleepTimeRemaining.value = 0L
    }

    fun setEqEnabled(enabled: Boolean) {
        _eqEnabled.value = enabled
        prefs.edit().putBoolean("eq_enabled", enabled).apply()
    }

    fun setEqBand(index: Int, value: Float) {
        val list = _eqBands.value.toMutableList()
        if (index in list.indices) {
            list[index] = value
            _eqBands.value = list
            prefs.edit().putString("eq_bands", list.joinToString(",")).apply()
        }
    }

    fun applyPreset(name: String) {
        _selectedPreset.value = name
        prefs.edit().putString("eq_preset", name).apply()
        val list = when (name) {
            "Bass Boost" -> listOf(12f, 8f, 2f, 0f, 0f)
            "Acoustic" -> listOf(4f, 2f, 4f, 6f, 4f)
            "Vocal" -> listOf(-2f, 2f, 8f, 6f, 2f)
            else -> listOf(0f, 0f, 0f, 0f, 0f) // Flat
        }
        _eqBands.value = list
        prefs.edit().putString("eq_bands", list.joinToString(",")).apply()
    }

    fun setBassBoost(value: Float) {
        _bassBoost.value = value
        prefs.edit().putFloat("bass_boost", value).apply()
    }

    fun setSurround(value: Float) {
        _surround.value = value
        prefs.edit().putFloat("surround", value).apply()
    }

    fun loadBookmarksForTrack(trackId: Long) {
        val str = prefs.getString("bookmarks_$trackId", "") ?: ""
        _bookmarks.value = if (str.isEmpty()) emptyList() else str.split(",").mapNotNull { it.toLongOrNull() }
    }

    fun addBookmark(trackId: Long, positionMs: Long) {
        val current = _bookmarks.value.toMutableList()
        if (!current.contains(positionMs)) {
            current.add(positionMs)
            current.sort()
            _bookmarks.value = current
            prefs.edit().putString("bookmarks_$trackId", current.joinToString(",")).apply()
        }
    }

    fun deleteBookmark(trackId: Long, positionMs: Long) {
        val current = _bookmarks.value.toMutableList()
        if (current.remove(positionMs)) {
            _bookmarks.value = current
            prefs.edit().putString("bookmarks_$trackId", current.joinToString(",")).apply()
        }
    }

    fun loadRecentSearches() {
        val set = prefs.getStringSet("recent_searches", setOf("AR Rahman", "Ed Sheeran", "Workout Mix", "Shape of You", "Beatles")) ?: emptySet()
        _recentSearches.value = set.toList()
    }

    fun addRecentSearch(query: String) {
        if (query.isBlank()) return
        val current = _recentSearches.value.toMutableSet()
        current.remove(query)
        current.add(query)
        _recentSearches.value = current.toList()
        prefs.edit().putStringSet("recent_searches", current).apply()
    }

    fun removeRecentSearch(query: String) {
        val current = _recentSearches.value.toMutableSet()
        current.remove(query)
        _recentSearches.value = current.toList()
        prefs.edit().putStringSet("recent_searches", current).apply()
    }

    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
        prefs.edit().putStringSet("recent_searches", emptySet()).apply()
    }

    fun seekForward() {
        player?.let { p ->
            val duration = p.duration
            val increment = _seekDurationSeconds.value * 1000L
            val target = if (duration > 0) {
                (p.currentPosition + increment).coerceAtMost(duration)
            } else {
                p.currentPosition + increment
            }
            p.seekTo(target)
        }
    }

    fun seekBackward() {
        player?.let { p ->
            val increment = _seekDurationSeconds.value * 1000L
            val target = (p.currentPosition - increment).coerceAtLeast(0L)
            p.seekTo(target)
        }
    }
    
    fun toggleShuffle() {
        player?.let { p ->
            p.shuffleModeEnabled = !p.shuffleModeEnabled
            _isShuffleEnabled.value = p.shuffleModeEnabled
        }
    }
    
    fun toggleRepeat() {
        player?.let { p ->
            val nextMode = when (p.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
            p.repeatMode = nextMode
            _repeatMode.value = nextMode
        }
    }
    
    fun nextTrack() {
        player?.let { p ->
            if (p.hasNextMediaItem()) {
                p.seekToNextMediaItem()
            }
        }
    }
    
    fun previousTrack() {
        player?.let { p ->
            if (p.hasPreviousMediaItem()) {
                p.seekToPreviousMediaItem()
            } else {
                p.seekTo(0)
            }
        }
    }

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    fun navigateToTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchFilter = MutableStateFlow("All")
    val searchFilter: StateFlow<String> = _searchFilter.asStateFlow()

    fun setSearchQuery(query: String, filter: String = "All") {
        _searchQuery.value = query
        _searchFilter.value = filter
    }

    fun updateTrackTags(trackId: Long, title: String, artist: String, album: String) {
        prefs.edit().apply {
            putString("override_title_$trackId", title)
            putString("override_artist_$trackId", artist)
            putString("override_album_$trackId", album)
            apply()
        }
        _tracks.value = _tracks.value.map {
            if (it.id == trackId) {
                it.copy(title = title, artist = artist, album = album)
            } else {
                it
            }
        }
        _currentTrack.value?.let {
            if (it.id == trackId) {
                _currentTrack.value = it.copy(title = title, artist = artist, album = album)
            }
        }
        _historyTrigger.value = _historyTrigger.value + 1
    }

    fun shareTrack(track: AudioTrack) {
        val context = getApplication<Application>()
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "audio/*"
            putExtra(android.content.Intent.EXTRA_STREAM, track.uri)
            putExtra(android.content.Intent.EXTRA_SUBJECT, track.title)
            putExtra(android.content.Intent.EXTRA_TEXT, "Listen to ${track.title} by ${track.artist}")
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            val chooser = android.content.Intent.createChooser(intent, "Share Song").apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            val textIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, "Listen to ${track.title} by ${track.artist}")
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(android.content.Intent.createChooser(textIntent, "Share Info").apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

    fun setAsRingtone(track: AudioTrack) {
        val context = getApplication<Application>()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (!android.provider.Settings.System.canWrite(context)) {
                val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                    data = Uri.parse("package:" + context.packageName)
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(context, "Please grant Write Settings permission, then try again.", android.widget.Toast.LENGTH_LONG).show()
                }
                return
            }
        }
        try {
            android.media.RingtoneManager.setActualDefaultRingtoneUri(
                context,
                android.media.RingtoneManager.TYPE_RINGTONE,
                track.uri
            )
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                android.widget.Toast.makeText(context, "Ringtone set successfully!", android.widget.Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                android.widget.Toast.makeText(context, "Failed to set ringtone: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    fun deleteTrack(track: AudioTrack) {
        val context = getApplication<Application>()
        val deletedSet = prefs.getStringSet("deleted_tracks", emptySet())?.toMutableSet() ?: mutableSetOf()
        deletedSet.add(track.id.toString())
        prefs.edit().putStringSet("deleted_tracks", deletedSet).apply()
        
        _tracks.value = _tracks.value.filter { it.id != track.id }
        if (_currentTrack.value?.id == track.id) {
            player?.stop()
            _currentTrack.value = null
            _isPlaying.value = false
        }
        
        val playlistNames = prefs.getStringSet("playlists_names", emptySet()) ?: emptySet()
        playlistNames.forEach { name ->
            val currentIds = prefs.getString("playlist_$name", "") ?: ""
            if (currentIds.isNotEmpty()) {
                val list = currentIds.split(",").toMutableList()
                if (list.remove(track.id.toString())) {
                    prefs.edit().putString("playlist_$name", list.joinToString(",")).apply()
                }
            }
        }
        
        player?.let { p ->
            for (i in 0 until p.mediaItemCount) {
                if (p.getMediaItemAt(i).mediaId == track.id.toString()) {
                    p.removeMediaItem(i)
                    break
                }
            }
        }
        updateQueueFromPlayer()
        
        try {
            context.contentResolver.delete(track.uri, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        _historyTrigger.value = _historyTrigger.value + 1
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            android.widget.Toast.makeText(context, "${track.title} removed from library.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCleared() {
        super.onCleared()
        saveActiveQueueState()
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        player = null
    }
}
