package com.example.dnmotors.viewmodel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.dnmotors.utils.MessageNotificationUtil
import com.example.dnmotors.view.activity.MainActivity
import com.example.dnmotors.viewdealer.activity.DealerActivity
import com.example.domain.model.ChatItem
import com.example.domain.model.Message
import com.example.domain.usecase.ChatUseCases

class ChatViewModel(private val chatUseCases: ChatUseCases) : ViewModel() {

    private val _chatItems = MutableLiveData<List<ChatItem>>()
    val chatItems: LiveData<List<ChatItem>> = _chatItems

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    fun loadChatList(isDealer: Boolean) {
        chatUseCases.loadChatList(isDealer).observeForever {
            _chatItems.postValue(it)
        }
    }

    fun loadMessages(chatId: String) {
        chatUseCases.loadMessages(chatId).observeForever {
            _messages.postValue(it)
        }
    }

    fun sendMessage(message: Message, chatId: String) {
        chatUseCases.sendMessage(message, chatId)
    }

    fun sendMediaMessage(message: Message, chatId: String) {
        chatUseCases.sendMediaMessage(message, chatId)
    }

    fun observeMessages(chatId: String, context: Context) {
        chatUseCases.observeMessages(chatId, context)
    }

    fun observeNewMessages(
        chatId: String,
        context: Context,
        activityClass: Class<out Activity>
    ) {
        chatUseCases.observeNewMessages(chatId).observeForever { message ->
            if (activityClass == MainActivity::class.java) {
                MessageNotificationUtil.createNotification(
                    context,
                    message,
                    MainActivity::class.java
                ) {
                    putExtra("carId", message.carId)
                    putExtra("dealerId", message.dealerId)
                }
            } else if (activityClass == DealerActivity::class.java) {
                MessageNotificationUtil.createNotification(
                    context,
                    message,
                    DealerActivity::class.java
                ) {
                    putExtra("userId", message.senderId)
                    putExtra("carId", message.carId)
                }
            }
        }
    }


}


sealed class ChatResult {
    object Success : ChatResult()
    data class Error(val message: String) : ChatResult()
    object Loading : ChatResult()
}
