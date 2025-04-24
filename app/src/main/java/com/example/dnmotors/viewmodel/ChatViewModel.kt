package com.example.dnmotors.viewmodel

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnmotors.R
import com.example.dnmotors.utils.MessageNotificationUtil
import com.example.domain.model.ChatItem
import com.example.domain.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import java.util.UUID

class ChatViewModel : ViewModel() {
    private val _chatItems = MutableLiveData<List<ChatItem>>()
    val chatItems: LiveData<List<ChatItem>> = _chatItems

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val baseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("messages")

    private var messagesListener: ValueEventListener? = null
    private var messagesListenerRef: DatabaseReference? = null


    fun loadChatListForDealer() {
        val dealerId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("chats")
            .whereEqualTo("dealerId", dealerId)
            .get()
            .addOnSuccessListener { result ->
                val chatItems = result.documents.mapNotNull { doc ->
                    val carId = doc.getString("carId")
                    val userId = doc.getString("userId")
                    if (carId != null && userId != null) {
                        ChatItem(
                            carId = carId,
                            userId = userId,
                            dealerId = dealerId
                        )
                    } else null
                }
                _chatItems.postValue(chatItems)
            }
            .addOnFailureListener { error ->
                println("Error loading dealer chat list: ${error.message}")
                _chatItems.postValue(emptyList())
            }
    }


    fun loadMessages(chatId: String) {
        val messagesRef = FirebaseFirestore.getInstance()
            .collection("chats")
            .document(chatId)
            .collection("messages")

        messagesRef
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatViewModel", "Error loading messages", error)
                    _messages.postValue(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val messages = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Message::class.java)?.let { message ->
                            val decodedMessage = try {
                                val bytes = Base64.decode(message.message, Base64.DEFAULT)
                                String(bytes, Charsets.UTF_8)
                            } catch (e: Exception) {
                                "[Error decoding message]"
                            }

                            message.copy(message = decodedMessage)
                        }
                    }

                    _messages.postValue(messages)
                } else {
                    _messages.postValue(emptyList())
                }
            }
    }


    fun sendMessage(
        chatId: String,
        messageText: String,
        senderId: String,
        senderName: String,
        userId: String,
        carId: String,
        isNotificationSent: Boolean
    ) {
        if (messageText.isBlank()) return

        val encodedMessage = Base64.encodeToString(messageText.toByteArray(Charsets.UTF_8), Base64.DEFAULT)

        val messageId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        val message = Message(
            id = messageId,
            senderId = senderId,
            name = senderName,
            message = encodedMessage,
            messageType = "text",
            timestamp = timestamp,
            carId = carId,
            isNotificationSent = false
        )

        val firestore = FirebaseFirestore.getInstance()

        // 1. Send the actual message
        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)
            .set(message)
            .addOnSuccessListener {
                // 2. Update the metadata of the chat document
                val chatMetadata = mapOf(
                    "userId" to userId,
                    "dealerId" to senderId,
                    "carId" to carId,
                    "lastMessage" to messageText,
                    "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                    "isNotificationSent" to isNotificationSent
                )

                firestore.collection("chats")
                    .document(chatId)
                    .set(chatMetadata, SetOptions.merge())
            }
            .addOnFailureListener {
                Log.e("ChatViewModel", "Failed to send message", it)
            }
    }

    private fun removeMessagesListener() {
        messagesListener?.let { listener ->
            messagesListenerRef?.removeEventListener(listener)
        }
        messagesListener = null
        messagesListenerRef = null
    }

    fun observeMessages(chatId: String, context: Context) {
        MessageNotificationUtil.observeNewMessages(chatId, context) { newMessage ->
            val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()
            currentMessages.add(newMessage)
            _messages.postValue(currentMessages)
        }
    }


    override fun onCleared() {
        super.onCleared()
        removeMessagesListener()
    }
}