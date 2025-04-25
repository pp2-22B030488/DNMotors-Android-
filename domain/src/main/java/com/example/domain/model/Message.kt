package com.example.domain.model

data class Message(
    val id: String? = null,
    val name: String? = null,
    var senderId: String? = null,
    val carId: String? = null,
    val message: String? = null,
    val messageType: String? = null,
    val base64: String? = null,
    val timestamp: Long = 0,
    val notificationSent: Boolean = false
)
