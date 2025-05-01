package com.example.dnmotors.viewdealer.compose.screen

import android.app.Activity
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import com.example.dnmotors.viewdealer.repository.DealerAudioRepository
import com.example.domain.model.Message
import com.example.domain.repository.MediaRepository
import java.util.UUID

@Composable
fun MessagesScreen(
    chatId: String,
    carId: String,
    userId: String,
    dealerId: String,
    dealerName: String,
    viewModel: ChatViewModel,
    onToggleBottomBar: (Boolean) -> Unit
) {
    val messages by viewModel.messages.observeAsState(emptyList())
    var input by remember { mutableStateOf("") }
    val context = LocalContext.current
    val mediaRepository = remember { MediaRepository(context) }
    var isRecording by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val activity = LocalContext.current as? Activity

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            DealerAudioRepository.startRecording(mediaRepository, context) {
                isRecording = it
            }
        } else {
            Toast.makeText(context, "Microphone permission is required to record audio", Toast.LENGTH_SHORT).show()
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
            if (mediaRepository.isRecording()) {
                mediaRepository.cleanupRecorder()
            }
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
                .background(Color.LightGray)
        ) {
            items(messages) { msg ->
                val isCurrentUser = msg.senderId == dealerId
                val alignment = if (isCurrentUser) Alignment.End else Alignment.Start
                val bubbleColor = if (isCurrentUser) Color(0xFFA5D6A7) else Color(0xFF90CAF9)
                val bubbleShape = RoundedCornerShape(12.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
                ) {
                    Column(
                        modifier = Modifier
                            .background(bubbleColor, bubbleShape)
                            .padding(8.dp)
                            .widthIn(max = 280.dp)
                    ) {
                        when (msg.messageType) {
                            "text" -> Text("${msg.name}: ${msg.text}")
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
                                                .padding(vertical = 4.dp)
                                        )
                                    } ?: Text("[Image not available]")
                                }
                            }
                            "audio" -> {
                                Text("${msg.name}:")
                                msg.mediaData?.let { AudioPlayer(it) } ?: Text("[Audio not available]")
                            }
                            "video" -> {
                                Text("${msg.name}:")
                                msg.mediaData?.let { VideoPlayer(it) } ?: Text("[Video not available]")
                            }
                            else -> Text("[Unsupported message type]")
                        }
                    }
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

            IconButton(
                onClick = {
                    activity?.let {
                        mediaRepository.requestAudioPermission(
                            activity = it,
                            permissionLauncher = permissionLauncher,
                            onPermissionGranted = {
                                if (isRecording) {
                                    DealerAudioRepository.stopRecording(mediaRepository, scope, viewModel, chatId, dealerId, dealerName, userId, carId, context) {
                                        isRecording = false
                                    }
                                } else {
                                    DealerAudioRepository.startRecording(mediaRepository, context) {
                                        isRecording = it
                                    }
                                }
                            },
                            onPermissionDenied = {
                                Toast.makeText(context, "Microphone permission is required to record audio", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
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

                        val message = Message(
                            id = UUID.randomUUID().toString(),
                            senderId = dealerId,
                            dealerId = dealerId,
                            userId = userId,
                            name = dealerName,
                            text = input.trim(),
                            messageType = "text",
                            timestamp = System.currentTimeMillis(),
                            carId = carId,
                            notificationSent = false
                        )
                        viewModel.sendMessage(message, chatId)

                        input = ""
                    }
                }
            ) {
                Text("Send")
            }
        }
    }
}
