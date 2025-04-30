package com.example.domain.usecase

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.domain.model.ChatItem
import com.example.domain.model.Message
import com.example.domain.repository.ChatRepository

class ChatUseCases(private val chatRepository: ChatRepository) {

    fun loadChatList(isDealer: Boolean): LiveData<List<ChatItem>> {
        return chatRepository.loadChatList(isDealer)
    }

    fun loadMessages(chatId: String): LiveData<List<Message>> {
        return chatRepository.loadMessages(chatId)
    }

    fun sendMessage(message: Message, chatId: String) {
        chatRepository.sendMessage(message, chatId)
    }

    fun sendMediaMessage(message: Message, chatId: String) {
        chatRepository.sendMediaMessage(message, chatId)
    }

    fun observeMessages(chatId: String, context: Context) {
        chatRepository.observeMessages(chatId, context)
    }

    fun observeNewMessages(chatId: String): LiveData<Message> {
        return chatRepository.observeNewMessages(chatId)
    }

}
