//package com.example.domain.usecase
//
//import com.example.domain.model.Chat
//import com.example.domain.model.Message
//
//interface ChatUseCase {
//    suspend fun sendMessage(chatId: String, message: Message)
//    suspend fun getUserChats(userId: String): List<Chat>
//    fun listenToMessages(chatId: String, onUpdate: (List<Message>) -> Unit)
//}
