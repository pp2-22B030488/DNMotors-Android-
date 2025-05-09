package com.example.dnmotors.viewmodel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
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
//
//    private val _chatItems = MediatorLiveData<List<ChatItem>>()
//    val chatItems: LiveData<List<ChatItem>> get() = _chatItems
//
//    private val _messages = MediatorLiveData<List<Message>>()
//    val messages: LiveData<List<Message>> get() = _messages
//
//    private var chatListSource: LiveData<List<ChatItem>>? = null
//    private var messageListSource: LiveData<List<Message>>? = null

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
//    fun loadChatList(isDealer: Boolean) {
//        chatListSource?.let { _chatItems.removeSource(it) }
//
//        val source = chatUseCases.loadChatList(isDealer)
//        chatListSource = source
//        _chatItems.addSource(source) {
//            _chatItems.value = it
//        }
//    }
//
//    fun loadMessages(chatId: String) {
//        messageListSource?.let { _messages.removeSource(it) }
//
//        val source = chatUseCases.loadMessages(chatId)
//        messageListSource = source
//        _messages.addSource(source) {
//            _messages.value = it
//        }
//    }

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
                    putExtra("userId", message.userId)
                }
            } else if (activityClass == DealerActivity::class.java) {
                MessageNotificationUtil.createNotification(
                    context,
                    message,
                    DealerActivity::class.java
                ) {
                    putExtra("userId", message.userId)
                    putExtra("carId", message.carId)
                    putExtra("dealerId", message.dealerId)
                }
            }
        }
    }

//    fun observeNewMessages(
//        chatId: String,
//        context: Context,
//        activityClass: Class<out Activity>
//    ) {
//        val newMessageLiveData = chatUseCases.observeNewMessages(chatId)
//
//        _messages.addSource(newMessageLiveData) { message ->
//            if (message != null) {
//                if (activityClass == MainActivity::class.java) {
//                    MessageNotificationUtil.createNotification(
//                        context,
//                        message,
//                        MainActivity::class.java
//                    ) {
//                        putExtra("carId", message.carId)
//                        putExtra("dealerId", message.dealerId)
//                    }
//                } else if (activityClass == DealerActivity::class.java) {
//                    MessageNotificationUtil.createNotification(
//                        context,
//                        message,
//                        DealerActivity::class.java
//                    ) {
//                        putExtra("userId", message.senderId)
//                        putExtra("carId", message.carId)
//                    }
//                }
//            }
//        }
//    }
//
//    override fun onCleared() {
//        chatListSource?.let { _chatItems.removeSource(it) }
//        messageListSource?.let { _messages.removeSource(it) }
//        super.onCleared()
//    }
}


sealed class ChatResult {
    object Success : ChatResult()
    data class Error(val message: String) : ChatResult()
    object Loading : ChatResult()
}
