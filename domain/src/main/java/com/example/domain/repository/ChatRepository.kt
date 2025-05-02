package com.example.domain.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.domain.model.ChatItem
import com.example.domain.model.Message

interface ChatRepository {
    fun loadChatList(isDealer: Boolean): LiveData<List<ChatItem>>
    fun loadMessages(chatId: String): LiveData<List<Message>>
    fun sendMessage(message: Message, chatId: String)
    fun sendMediaMessage(message: Message, chatId: String)
    fun observeMessages(chatId: String, context: Context)
    fun observeNewMessages(chatId: String): LiveData<Message>
}
