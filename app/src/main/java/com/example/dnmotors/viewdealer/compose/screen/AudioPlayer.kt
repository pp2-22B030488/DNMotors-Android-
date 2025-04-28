package com.example.dnmotors.viewdealer.compose.screen

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AudioPlayer(audioFilePath: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isPlaying by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        IconButton(
            onClick = {
                if (isLoading) return@IconButton

                scope.launch {
                    if (isPlaying && mediaPlayer != null) {
                        // Stop playback
                        mediaPlayer?.apply {
                            stop()
                            reset()
                        }
                        isPlaying = false
                    } else {
                        isLoading = true
                        error = null

                        try {
                            withContext(Dispatchers.IO) {
                                try {
                                    mediaPlayer?.release()

                                    mediaPlayer = MediaPlayer().apply {
                                        setDataSource(audioFilePath)
                                        setAudioAttributes(
                                            AudioAttributes.Builder()
                                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                                .build()
                                        )
                                        setOnPreparedListener {
                                            it.start()
                                            isPlaying = true
                                            isLoading = false
                                        }
                                        setOnCompletionListener {
                                            isPlaying = false
                                        }
                                        setOnErrorListener { _, what, extra ->
                                            val errorMsg = "MediaPlayer error: what=$what, extra=$extra"
                                            Log.e("AudioPlayer", errorMsg)
                                            error = errorMsg
                                            isPlaying = false
                                            isLoading = false
                                            true
                                        }
                                        prepareAsync()
                                    }
                                } catch (e: Exception) {
                                    Log.e("AudioPlayer", "MediaPlayer error", e)
                                    withContext(Dispatchers.Main) {
                                        error = "Error playing audio: ${e.message}"
                                        isLoading = false
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("AudioPlayer", "Failed to play audio", e)
                            error = "Error playing audio: ${e.message}"
                            isLoading = false
                        }
                    }
                }
            },
            enabled = !isLoading
        ) {
            when {
                isPlaying -> Icon(Icons.Default.Stop, contentDescription = "Stop Audio")
                else -> Icon(Icons.Default.PlayArrow, contentDescription = "Play Audio")
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = when {
                    isLoading -> "Preparing audio..."
                    isPlaying -> "Playing audio..."
                    else -> "Audio Message"
                },
                style = MaterialTheme.typography.bodyMedium
            )

            if (error != null) {
                Text(
                    text = error!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    LaunchedEffect(error) {
        if (error != null) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }
}

