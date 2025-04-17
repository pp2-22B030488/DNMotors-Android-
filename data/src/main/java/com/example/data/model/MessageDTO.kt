package com.example.data.model

data class MessageDTO(
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)