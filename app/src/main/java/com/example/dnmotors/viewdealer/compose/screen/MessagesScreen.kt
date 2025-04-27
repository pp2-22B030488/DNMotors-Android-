package com.example.dnmotors.viewdealer.compose.screen

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.dnmotors.utils.MediaUtils
import com.example.dnmotors.viewdealer.repository.DealerAudioRepository
import com.example.domain.repository.MediaRepoUseCase
import com.example.domain.repository.MediaRepository
import java.io.File

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
    var isRecording by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val activity = LocalContext.current as? Activity
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var videoUri by remember { mutableStateOf<Uri?>(null) }

    // Variables to hold the latest file references
    var imageFile by remember { mutableStateOf<File?>(null) }
    var videoFile by remember { mutableStateOf<File?>(null) }


    // Constants for permission request code
    val REQUEST_CAMERA_PERMISSION = 1001
    val REQUEST_STORAGE_PERMISSION = 1002

    // Function to check and request permissions
    fun checkAndRequestPermissions(context: Context): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        val storagePermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val permissionsNeeded = mutableListOf<String>()

        if (!cameraPermission) {
            permissionsNeeded.add(Manifest.permission.CAMERA)
        }

        if (!storagePermission) {
            permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        return if (permissionsNeeded.isNotEmpty()) {
            // Request permissions if not granted
            ActivityCompat.requestPermissions(
                context as Activity, permissionsNeeded.toTypedArray(), REQUEST_CAMERA_PERMISSION
            )
            false
        } else {
            true
        }
    }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageFile?.let { file ->
                if (file.exists()) {
                    val bytes = file.readBytes()
                    val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    viewModel.sendMediaMessage(
                        chatId = chatId,
                        base64Media = base64,
                        type = "image",
                        senderId = dealerId,
                        senderName = dealerName,
                        carId = carId
                    )
                } else {
                    // Log or show error
                    Toast.makeText(context, "Image file does not exist", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                // Log or show error if imageFile is null
                Toast.makeText(context, "No image file found", Toast.LENGTH_SHORT).show()
            }
        }
    }


    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success) {
            videoFile?.let { file ->
                if (file.exists()) {
                    val bytes = file.readBytes()
                    val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    viewModel.sendMediaMessage(
                        chatId = chatId,
                        base64Media = base64,
                        type = "video",
                        senderId = dealerId,
                        senderName = dealerName,
                        carId = carId
                    )
                } else {
                    // Log or show error
                    Toast.makeText(context, "Video file does not exist", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                // Log or show error if videoFile is null
                Toast.makeText(context, "No video file found", Toast.LENGTH_SHORT).show()
            }
        }
    }


    IconButton(onClick = {
        if (checkAndRequestPermissions(context)) {
            val file = MediaRepoUseCase.createImageFile(context)
            imageFile = file
            file?.let {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", it)
                photoLauncher.launch(uri)
            }
        }
    }) {
        Icon(Icons.Default.PhotoCamera, contentDescription = "Take Picture")
    }

    IconButton(onClick = {
        if (checkAndRequestPermissions(context)) {
            val file = MediaRepoUseCase.createVideoFile(context)
            videoFile = file
            file?.let {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", it)
                videoLauncher.launch(uri)
            }
        }
    }) {
        Icon(Icons.Default.Videocam, contentDescription = "Record Video")
    }


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
        ) {
            items(messages) { msg ->
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
                                        .padding(vertical = 8.dp)
                                )
                            } ?: Text("${msg.name}: [Image not available]")
                        }
                    }
                    "audio" -> {
                        msg.mediaData?.let {
                            AudioPlayer(it)
                        } ?: Text("${msg.name}: [Audio not available]")
                    }
                    "video" -> {
                        msg.mediaData?.let {
                            VideoPlayer(it)
                        } ?: Text("${msg.name}: [Video not available]")
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
            IconButton(onClick = {
                imageFile = MediaRepoUseCase.createImageFile(context)
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile!!)
                photoLauncher.launch(uri)
            }) {
                Icon(Icons.Default.PhotoCamera, contentDescription = "Take Picture")
            }

            IconButton(onClick = {
                videoFile = MediaRepoUseCase.createVideoFile(context)
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", videoFile!!)
                videoLauncher.launch(uri)
            }) {
                Icon(Icons.Default.Videocam, contentDescription = "Record Video")
            }



            // Audio record button
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
