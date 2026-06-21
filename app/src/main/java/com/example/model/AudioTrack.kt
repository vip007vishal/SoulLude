package com.example.model

import android.net.Uri

data class AudioTrack(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val uri: Uri,
    val albumArtUri: Uri? = null,
    val genre: String = "Unknown Genre",
    val composer: String = "Unknown Composer",
    val year: String = "Unknown Year",
    val folder: String = "Unknown Folder",
    val size: Long = 0L
)
