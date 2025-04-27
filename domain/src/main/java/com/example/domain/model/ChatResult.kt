package com.example.domain.model

sealed class ChatResult {
    object Success : ChatResult()
    data class Error(val message: String) : ChatResult()
    object Loading : ChatResult()
}