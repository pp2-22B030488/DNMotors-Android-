package com.example.domain.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
//@Entity(
//    tableName = "messages",
//    foreignKeys = [ForeignKey(
//        entity = ChatItem::class,
//        parentColumns = ["chatId"],
//        childColumns = ["chatId"],
//        onDelete = ForeignKey.CASCADE
//    )],
//    indices = [Index("chatId")]
//)
data class Message(
//    @PrimaryKey
    val id: String? = null,
//    val chatId: String? = null,
    val name: String? = null,
    var senderId: String? = null,
    var userId: String? = null,
    var dealerId: String? = null,
    val carId: String? = null,
    val text: String? = null,
    val mediaData: String? = null,
    val messageType: String? = null,
    val timestamp: Long = 0,
    val notificationSent: Boolean = false
) : Parcelable