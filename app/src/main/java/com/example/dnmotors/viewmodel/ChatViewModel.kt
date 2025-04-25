package com.example.dnmotors.viewmodel

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
                    val timestamp = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L

                    if (carId != null && userId != null) {
                        ChatItem(
                            carId = carId,
                            userId = userId,
                            dealerId = dealerId,
                            timestamp = timestamp
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
                            val decodedMessage = when (message.messageType) {
                                "text" -> {
                                    val bytes = Base64.decode(message.message, Base64.DEFAULT)
                                    String(bytes, Charsets.UTF_8)
                                }
                                "image", "audio", "video" -> message.message // Still base64, handled by decoder in UI
                                else -> "[Unknown type]"
                            }


                            // Mark as notified if not already
                            if (!message.notificationSent) {
                                doc.reference.update("notificationSent", true)
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
        notificationSent: Boolean
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
            notificationSent = false
        )
        val firestore = FirebaseFirestore.getInstance()

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
                    "timestamp" to FieldValue.serverTimestamp(),
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
        FirebaseFirestore.getInstance()
            .collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || snapshot.isEmpty) return@addSnapshotListener

                for (doc in snapshot.documents) {
                    val message = doc.toObject(Message::class.java) ?: continue
                    if (!message.notificationSent) {
                        // Send notification
                        MessageNotificationUtil.sendNotification(context, message)

                        // Mark message as notified
                        doc.reference.update("notificationSent", true)
                    }
                }
            }
    }
    fun sendMediaMessage(
        chatId: String,
        base64Media: String,
        type: String,
        senderId: String,
        senderName: String,
        userId: String,
        carId: String
    ) {
        val message = Message(
            message = base64Media,
            messageType = type,
            senderId = senderId,
            name = senderName,
            timestamp = System.currentTimeMillis()
        )

        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(message)
    }

    override fun onCleared() {
        super.onCleared()
        removeMessagesListener()
    }
}