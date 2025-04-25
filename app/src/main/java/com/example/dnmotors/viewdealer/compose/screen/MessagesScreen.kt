package com.example.dnmotors.viewdealer.compose.screen

import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.ui.input.pointer.pointerInput
import com.example.dnmotors.viewdealer.compose.MediaPlayer.handleMediaMessage
import java.io.File
import java.util.UUID

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

    val launcherImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // Convert image uri to base64 and send
            handleMediaMessage(
                uri = uri,
                type = "image",
                viewModel = viewModel,
                chatId = chatId,
                dealerId = dealerId,
                dealerName = dealerName,
                userId = userId,
                carId = carId,
                context = context
            )
        }
    }

    val launcherFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // Convert file uri to base64 and send
            handleMediaMessage(
                uri = uri,
                type = "file",
                viewModel = viewModel,
                chatId = chatId,
                dealerId = dealerId,
                dealerName = dealerName,
                userId = userId,
                carId = carId,
                context = context
            )
        }
    }

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
                    "text" -> Text("${msg.name}: ${msg.message}")
                    "image" -> {
                        msg.message?.let {
                            val imageBytes = Base64.decode(it, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            Image(bitmap = bitmap.asImageBitmap(), contentDescription = null)
                        } ?: Text("${msg.name}: [Image not available]")
                    }
                    "audio" -> {
                        msg.message?.let {
                            val audioBytes = Base64.decode(it, Base64.DEFAULT)
                            AudioPlayer(audioBytes)
                        } ?: Text("${msg.name}: [Audio not available]")
                    }
                    "file" -> {
                        msg.message?.let {
                            Text("${msg.name}: [File attached]")
                        } ?: Text("${msg.name}: [File missing]")
                    }

                    else -> Text("${msg.name}: [Unsupported]")
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
            val audioFile = remember {
                File(context.cacheDir, "${UUID.randomUUID()}.3gp")
            }

            var isRecording by remember { mutableStateOf(false) }
            val mediaRecorder = remember {
                MediaRecorder()
            }
            var expanded by remember { mutableStateOf(false) }
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.AttachFile, contentDescription = null)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Image, contentDescription = "Send Image", modifier = Modifier.padding(end = 8.dp))
                            Text("Send Image")
                        }
                    },
                    onClick = {
                        expanded = false
                        launcherImagePicker.launch("image/*")
                    }
                )

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Mic, contentDescription = "Record Audio", modifier = Modifier.padding(end = 8.dp))
                            Text("Hold to Record")
                        }
                    },
                    onClick = { /* Handled by pointerInput below */ },
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                try {
                                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                                    mediaRecorder.setOutputFile(audioFile.absolutePath)
                                    mediaRecorder.prepare()
                                    mediaRecorder.start()
                                    isRecording = true
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }

                                // Wait for release
                                tryAwaitRelease()

                                if (isRecording) {
                                    try {
                                        mediaRecorder.stop()
                                        mediaRecorder.reset()
                                        isRecording = false

                                        // Send recorded audio
                                        val bytes = audioFile.readBytes()
                                        val base64 = Base64.encodeToString(bytes, Base64.DEFAULT)
                                        handleMediaMessage(
                                            uri = Uri.fromFile(audioFile),
                                            base64 = base64,
                                            type = "audio",
                                            viewModel = viewModel,
                                            chatId = chatId,
                                            dealerName = dealerName,
                                            dealerId = dealerId,
                                            userId = userId,
                                            carId = carId,
                                            context = context
                                            )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }

                                expanded = false
                            }
                        )
                    }
                )


                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AttachFile, contentDescription = "Send File", modifier = Modifier.padding(end = 8.dp))
                            Text("Send File")
                        }
                    },
                    onClick = {
                        expanded = false
                        launcherFilePicker.launch("*/*")
                    }
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
fun AudioPlayer(audioBytes: ByteArray) {
    val context = LocalContext.current
    val tempFile = remember(audioBytes) {
        File.createTempFile("temp_audio", ".mp3", context.cacheDir).apply {
            writeBytes(audioBytes)
        }
    }

    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }
    var isPlaying by remember { mutableStateOf(false) }

    DisposableEffect(tempFile.absolutePath) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(tempFile.absolutePath)
            prepare()
        }

        onDispose {
            mediaPlayer?.release()
            tempFile.delete()
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = {
            mediaPlayer?.let {
                if (isPlaying) {
                    it.pause()
                } else {
                    it.start()
                }
                isPlaying = !isPlaying
            }
        }) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play"
            )
        }
        Text(text = if (isPlaying) "Playing..." else "Paused")
    }
}
