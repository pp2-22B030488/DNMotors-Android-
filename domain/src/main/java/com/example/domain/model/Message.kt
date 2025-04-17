package com.example.domain.model

data class Message(
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long
)