package com.example.domain.model

data class Message(
    val id: String? = null,
    val name: String? = null,
    val message: String? = null,
    val mediaUrl: String? = null,
    val mediaType: String? = null,
    val base64: String? = null,
    val timestamp: Long = 0
)