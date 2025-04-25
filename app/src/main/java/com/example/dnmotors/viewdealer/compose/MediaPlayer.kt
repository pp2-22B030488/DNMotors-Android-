package com.example.dnmotors.viewdealer.compose

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.example.dnmotors.viewmodel.ChatViewModel

object MediaPlayer {

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
        val finalBase64 = base64 ?: run {
            try {
                val inputStream = uri?.let { context.contentResolver.openInputStream(it) }
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                bytes?.let { Base64.encodeToString(it, Base64.DEFAULT) }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        if (finalBase64 != null) {
            viewModel.sendMediaMessage(
                chatId = chatId,
                base64Media = finalBase64,
                type = type,
                senderId = dealerId,
                senderName = dealerName,
                userId = userId,
                carId = carId
            )
        }
    }


}