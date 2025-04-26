package com.example.dnmotors.viewdealer.compose.screen

import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.dnmotors.viewmodel.ChatViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.ui.input.pointer.pointerInput
import com.example.dnmotors.utils.MediaUtils
import com.example.dnmotors.viewdealer.compose.ChatMediaPlayer
import com.example.domain.repository.MediaRepository
import com.example.domain.util.FileUtils
import java.io.File
import java.io.IOException

@Composable
fun MessagesScreen(
    chatId: String,
    carId: String,
    userId: String,
    dealerId: String,
    dealerName: String,
    viewModel: ChatViewModel = viewModel(),
    onToggleBottomBar: (Boolean) -> Unit
) {
    val messages by viewModel.messages.observeAsState(emptyList())
    var input by remember { mutableStateOf("") }
    val context = LocalContext.current
    val mediaRepository = remember { MediaRepository(context) }

    LaunchedEffect(chatId) {
        viewModel.loadMessages(chatId)
        viewModel.observeMessages(chatId, context)
    }

    DisposableEffect(Unit) {
        onToggleBottomBar(false)
        onDispose {
            onToggleBottomBar(true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(messages) { msg ->
                when (msg.messageType) {
                    "text" -> {
                        Text("${msg.name}: ${msg.text}")
                    }

                    "image" -> {
                        msg.mediaData?.let {
                            val decodedBitmap = runCatching {
                                val bytes = Base64.decode(it, Base64.NO_WRAP)
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            }.getOrNull()

                            decodedBitmap?.let { bitmap ->
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .padding(vertical = 8.dp)
                                )
                            } ?: Text("${msg.name}: [Image not available]")
                        }
                    }

                    "audio" -> {
                        msg.mediaData?.let {
                            AudioPlayer(it) // Pass Base64 string
                        } ?: Text("${msg.name}: [Audio not available]")
                    }

                    "file" -> {
                        Text("${msg.name}: [File attached]")
                    }

                    else -> Text("${msg.name}: [Unsupported message type]")
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var isRecording by remember { mutableStateOf(false) }
            val mediaRecorder = remember { MediaRecorder() }
            var audioFile: File? by remember { mutableStateOf(null) }

            IconButton(onClick = { /* Handle file attachment */ }) {
                Icon(Icons.Default.AttachFile, contentDescription = "Attach File")
            }

            IconButton(
                onClick = { /* Required but handled by gesture detector */ },

                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                try {
                                    // Start recording
                                    mediaRecorder.apply {
                                        setAudioSource(MediaRecorder.AudioSource.MIC)
                                        setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                                        setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                                        audioFile = FileUtils.createTempFile(context, "audio").also {
                                            if (it != null) {
                                                setOutputFile(it.absolutePath)
                                            }
                                        }
                                        prepare()
                                        start()
                                        isRecording = true
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Recording failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                                tryAwaitRelease()

                                if (isRecording) {
                                    mediaRecorder.stop()
                                    mediaRecorder.reset()
                                    audioFile?.let { file ->
                                        val base64 = FileUtils.fileToBase64(file.absolutePath)
                                        viewModel.sendMediaMessage(
                                            chatId = chatId,
                                            base64Media = base64,
                                            type = "audio",
                                            senderId = dealerId,
                                            senderName = dealerName,
                                            userId = userId,
                                            carId = carId
                                        )
                                    }
                                    isRecording = false
                                }
                            }
                        )
                    }
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Pause else Icons.Default.Mic,
                    contentDescription = if (isRecording) "Stop Recording" else "Record Audio"
                )
            }

            TextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (input.isNotBlank()) {
                        viewModel.sendMessage(
                            chatId = chatId,
                            messageText = input.trim(),
                            senderId = dealerId,
                            senderName = dealerName,
                            userId = userId,
                            carId = carId,
                            notificationSent = false
                        )
                        input = ""
                    }
                }
            ) {
                Text("Send")
            }
        }
    }
}

@Composable
fun AudioPlayer(base64Audio: String) {
    val context = LocalContext.current
    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    DisposableEffect(base64Audio) {
        val tempFile = MediaUtils.decodeBase64ToFile(base64Audio, "audio", context)
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(tempFile?.absolutePath ?: "")
                setOnPreparedListener { mp ->
                    // Update UI state if needed
                }
                setOnCompletionListener {
                    isPlaying = false
                }
                prepareAsync() // Non-blocking preparation
            } catch (e: IOException) {
                error = "Error preparing audio: ${e.message}"
            }
        }

        onDispose {
            mediaPlayer?.release()
            tempFile?.delete()
        }
    }

    if (error != null) {
        Text(text = error!!)
        return
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = {
                mediaPlayer?.let { player ->
                    if (isPlaying) {
                        player.pause()
                        isPlaying = false
                    } else {
                        player.start()
                        isPlaying = true
                    }
                }
            }
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play"
            )
        }
        Text(text = if (isPlaying) "Playing..." else "Tap to play")
    }
}