package com.example.domain.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

//@Entity(tableName = "chat_items")
@Parcelize
data class ChatItem(
//    @PrimaryKey
//    val chatId: String,
    val carId: String,
    val name: String,
    val dealerId: String,
    val userId: String,
    val timestamp: Long = 0L,
    val dealerName: String,
    val brand: String = "",
    val lastMessage: String,
    val messageTime: String,
    val imageUrl: List<String> = emptyList(),
    val model: String = "",
    val year: Int = 0,

): Parcelable
