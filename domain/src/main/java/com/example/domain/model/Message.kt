package com.example.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Message(
    val id: String? = null,
    val name: String? = null,
    var senderId: String? = null,
    val carId: String? = null,
    val text: String? = null,
    val mediaData: String? = null,
    val messageType: String? = null,
    val timestamp: Long = 0,
    val notificationSent: Boolean = false
) : Parcelable
