package com.example.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

//@Entity(tableName = "chat_items")
data class ChatItem(
//    @PrimaryKey
//    val chatId: String,
    val carId: String,
    val name: String,
    val dealerId: String,
    val userId: String,
    val timestamp: Long = 0L
)
