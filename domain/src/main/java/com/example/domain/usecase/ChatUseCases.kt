package com.example.domain.usecase

import com.example.domain.model.ChatItem
import com.example.domain.model.Message
import com.example.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class GetDealerChatsUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(dealerId: String): Result<List<ChatItem>> {
        return repository.loadChatListForDealer(dealerId)
    }
}

class GetChatMessagesUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(chatId: String): Flow<Result<List<Message>>> {
        return repository.loadMessages(chatId)
    }
}

class SendTextMessageUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(chatId: String, message: Message): Result<Unit> {
        return repository.sendTextMessage(chatId, message)
    }
}

class SendMediaMessageUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(chatId: String, message: Message): Result<Unit> {
        return repository.sendMediaMessage(chatId, message)
    }
}

class ObserveLatestMessageUseCase(private val repository: ChatRepository) {
    operator fun invoke(chatId: String, currentUserId: String): Flow<Message?> {
        return repository.observeLatestMessage(chatId, currentUserId)
    }
}