package com.example.dnmotors.viewdealer.repository

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.dnmotors.viewmodel.ChatViewModel
import com.example.domain.repository.MediaRepository
import com.example.domain.util.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal object DealerAudioRepository {
    fun startRecording(
        mediaRepository: MediaRepository,

        context: Context,
        onRecordingStateChanged: (Boolean) -> Unit
    ) {
        if (mediaRepository.isRecording()) return

        mediaRepository.startRecording(
            onStart = {
                onRecordingStateChanged(true)
                Toast.makeText(context, "Recording started...", Toast.LENGTH_SHORT).show()
            },
            onFailure = { errorMessage ->
                onRecordingStateChanged(false)
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }

    fun stopRecording(
        mediaRepository: MediaRepository,
        scope: CoroutineScope,
        viewModel: ChatViewModel,
        chatId: String,
        dealerId: String,
        dealerName: String,
        userId: String,
        carId: String,
        context: Context,
        onRecordingStateChanged: (Boolean) -> Unit
    ) {
        if (!mediaRepository.isRecording()) return

        mediaRepository.stopRecording(
            onSuccess = { file ->
                scope.launch {
                    try {
                        val base64 = withContext(Dispatchers.IO) {
                            FileUtils.fileToBase64(file)
                        }

                        viewModel.sendMediaMessage(
                            chatId = chatId,
                            base64Media = base64,
                            type = "audio",
                            senderId = dealerId,
                            senderName = dealerName,
                            carId = carId
                        )

                        onRecordingStateChanged(false)
                        Toast.makeText(context, "Audio message sent", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Failed to send audio: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("MessagesScreen", "Failed to send audio", e)
                        onRecordingStateChanged(false)
                    }
                }
            },
            onFailure = {
                Toast.makeText(context, "Recording failed", Toast.LENGTH_SHORT).show()
                onRecordingStateChanged(false)
            }
        )
    }

}
