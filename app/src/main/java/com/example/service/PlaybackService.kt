package com.example.service

import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.DefaultMediaNotificationProvider

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null
    
    private var equalizer: android.media.audiofx.Equalizer? = null
    private var bassBoost: android.media.audiofx.BassBoost? = null
    private var virtualizer: android.media.audiofx.Virtualizer? = null

    private val preferenceListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        try {
            when (key) {
                "eq_enabled" -> {
                    val enabled = sharedPreferences.getBoolean("eq_enabled", false)
                    equalizer?.enabled = enabled
                    bassBoost?.enabled = enabled
                    virtualizer?.enabled = enabled
                    
                    if (enabled) {
                        applyBands(sharedPreferences)
                        applyBassBoost(sharedPreferences)
                        applySurround(sharedPreferences)
                    }
                }
                "eq_bands" -> {
                    if (sharedPreferences.getBoolean("eq_enabled", false)) {
                        applyBands(sharedPreferences)
                    }
                }
                "bass_boost" -> {
                    if (sharedPreferences.getBoolean("eq_enabled", false)) {
                        applyBassBoost(sharedPreferences)
                    }
                }
                "surround" -> {
                    if (sharedPreferences.getBoolean("eq_enabled", false)) {
                        applySurround(sharedPreferences)
                    }
                }
                "seek_duration" -> {
                    // Handled dynamically in ViewModel seek control methods
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun applyBands(prefs: android.content.SharedPreferences) {
        val eq = equalizer ?: return
        val eqBandsStr = prefs.getString("eq_bands", "0,0,0,0,0") ?: "0,0,0,0,0"
        val bands = eqBandsStr.split(",").mapNotNull { it.toFloatOrNull() }
        val count = eq.numberOfBands.toInt().coerceAtMost(bands.size)
        val range = eq.bandLevelRange
        for (i in 0 until count) {
            val mB = (bands[i] * 100).toInt().coerceIn(range[0].toInt(), range[1].toInt())
            eq.setBandLevel(i.toShort(), mB.toShort())
        }
    }

    private fun applyBassBoost(prefs: android.content.SharedPreferences) {
        val bb = bassBoost ?: return
        val value = prefs.getFloat("bass_boost", 0f)
        bb.setStrength((value * 10).toInt().toShort())
    }

    private fun applySurround(prefs: android.content.SharedPreferences) {
        val v = virtualizer ?: return
        val value = prefs.getFloat("surround", 0f)
        v.setStrength((value * 10).toInt().toShort())
    }

    override fun onCreate() {
        super.onCreate()
        val prefs = getSharedPreferences("music_history", android.content.Context.MODE_PRIVATE)
        val seekDurationMs = prefs.getInt("seek_duration", 5) * 1000L
        
        player = ExoPlayer.Builder(this)
            .setSeekForwardIncrementMs(seekDurationMs)
            .setSeekBackIncrementMs(seekDurationMs)
            .build()
            
        player?.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                if (audioSessionId != android.media.AudioManager.AUDIO_SESSION_ID_GENERATE) {
                    try {
                        equalizer?.release()
                        bassBoost?.release()
                        virtualizer?.release()
                        
                        equalizer = android.media.audiofx.Equalizer(0, audioSessionId).apply {
                            enabled = prefs.getBoolean("eq_enabled", false)
                            applyBands(prefs)
                        }
                        
                        bassBoost = android.media.audiofx.BassBoost(0, audioSessionId).apply {
                            enabled = prefs.getBoolean("eq_enabled", false)
                            applyBassBoost(prefs)
                        }
                        
                        virtualizer = android.media.audiofx.Virtualizer(0, audioSessionId).apply {
                            enabled = prefs.getBoolean("eq_enabled", false)
                            applySurround(prefs)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
        
        prefs.registerOnSharedPreferenceChangeListener(preferenceListener)
        
        mediaSession = MediaSession.Builder(this, player!!).build()
        
        val provider = DefaultMediaNotificationProvider.Builder(this).build()
        setMediaNotificationProvider(provider)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0 || player.playbackState == Player.STATE_ENDED) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        equalizer?.release()
        bassBoost?.release()
        virtualizer?.release()
        getSharedPreferences("music_history", android.content.Context.MODE_PRIVATE)
            .unregisterOnSharedPreferenceChangeListener(preferenceListener)
        super.onDestroy()
    }
}
