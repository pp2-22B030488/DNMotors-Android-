package com.example.domain.usecase


import com.example.domain.model.Message
import com.example.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow

class GetMessagesUseCase(private val repository: MessageRepository) {
    operator fun invoke(chatId: String): Flow<List<Message>> = repository.getMessages(chatId)
}

class SendMessageUseCase(private val repository: MessageRepository) {
    suspend operator fun invoke(chatId: String, message: Message) =
        repository.sendMessage(chatId, message)
}