package com.example.domain.repository

import com.example.domain.model.Message

interface ChatRepository {
    suspend fun getChats(userId: String): List<String>
    suspend fun getMessages(userId: String, carId: String): List<Message>
    suspend fun sendMessage(userId: String, carId: String, message: Message)
}
