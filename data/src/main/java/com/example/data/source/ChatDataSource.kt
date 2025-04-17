package com.example.data.source

import com.example.data.model.MessageDTO
import kotlinx.coroutines.flow.Flow

interface ChatDataSource {
    fun getMessages(chatId: String): Flow<List<MessageDTO>>
    suspend fun sendMessage(chatId: String, message: MessageDTO)
}