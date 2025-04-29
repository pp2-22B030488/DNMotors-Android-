package com.example.domain.model

data class ChatItem(
    val carId: String,
    val name: String,
    val dealerId: String,
    val userId: String,
    val timestamp: Long = 0L
)
