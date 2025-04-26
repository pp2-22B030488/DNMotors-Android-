package com.example.dnmotors.viewdealer.compose

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.example.dnmotors.viewmodel.ChatViewModel

object ChatMediaPlayer {

    fun handleMediaMessage(
        uri: Uri? = null,
        base64: String? = null,
        type: String,
        viewModel: ChatViewModel,
        chatId: String,
        dealerId: String,
        dealerName: String,
        userId: String,
        carId: String,
        context: Context
    ) {
        val mediaBase64 = base64 ?: run {
            if (uri != null) {
                try {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val bytes = inputStream.readBytes()
                        Base64.encodeToString(bytes, Base64.NO_WRAP) // use NO_WRAP to avoid \n breaks
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            } else null
        }

        if (mediaBase64 != null) {
            viewModel.sendMediaMessage(
                chatId = chatId,
                base64Media = mediaBase64,
                type = type,
                senderId = dealerId,
                senderName = dealerName,
                userId = userId,
                carId = carId
            )
        } else {
            // Optionally, show an error: no media found
        }
    }


}