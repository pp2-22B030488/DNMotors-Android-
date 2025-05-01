package com.example.dnmotors.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dnmotors.viewmodel.ChatViewModel
import com.example.domain.usecase.ChatUseCases

class ChatViewModelFactory(private val chatUseCases: ChatUseCases) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(chatUseCases) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
