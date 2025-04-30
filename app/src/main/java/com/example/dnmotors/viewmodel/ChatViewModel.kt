package com.example.dnmotors.viewmodel

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnmotors.App
import com.example.domain.util.MediaUtils
import com.example.dnmotors.utils.MessageNotificationUtil
import com.example.domain.model.ChatItem
import com.example.domain.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel : ViewModel() {
    val _chatItems = MutableLiveData<List<ChatItem>>()
    val chatItems: LiveData<List<ChatItem>> = _chatItems
    private val appContext = App.context

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private var messagesListener: ValueEventListener? = null
    private var messagesListenerRef: DatabaseReference? = null


    fun loadChatList(isDealer: Boolean) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val queryField = if (isDealer) "dealerId" else "userId"

        FirebaseFirestore.getInstance()
            .collection("chats")
            .whereEqualTo(queryField, currentUserId)
            .get()
            .addOnSuccessListener { result ->
                val chatItems = result.documents.mapNotNull { doc ->
                    val carId = doc.getString("carId")
                    val fetcheduserId = doc.getString("userId")
                    val fetchedDealerId = doc.getString("dealerId")
                    val name = doc.getString("name")
                    val timestamp = doc.getLong("timestamp")

                    if (carId != null && fetcheduserId != null && name != null && timestamp != null && fetchedDealerId !=null) {
                        ChatItem(
                            carId = carId,
                            userId = fetcheduserId,
                            dealerId = fetchedDealerId,
                            timestamp = timestamp,
                            name = name
                        )
                    } else null
                }
                _chatItems.postValue(chatItems)
            }
            .addOnFailureListener { error ->
                println("Error loading chat list: ${error.message}")
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

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Message::class.java)?.let { message ->
                            when (message.messageType?.lowercase()) {
                                "text" -> handleTextMessage(message, doc.reference)
                                "audio", "video" -> handleAudioVideoMessage(message)
                                "image" -> handleImageMessage(message)
                                else -> message.copy(text = "[Unknown message type]")
                            }

                        }
                    } catch (e: Exception) {
                        Log.e("MessageDecode", "Error processing message ${doc?.id}", e)
                        null
                    }
                } ?: emptyList()

                _messages.postValue(messages)
            }
    }

    private fun handleTextMessage(message: Message, ref: DocumentReference): Message {
        return try {
            val decodedText = if (!message.mediaData.isNullOrEmpty()) {
                Base64.decode(message.mediaData, Base64.DEFAULT).toString(Charsets.UTF_8)
            } else {
                Log.w("MessageDecode", "Empty text field in message")
                "[Empty message]"
            }
            message.copy(text = decodedText)
        } catch (e: Exception) {
            Log.e("MessageDecode", "Base64 decoding error", e)
            message.copy(text = "[Decoding error]")
        }
    }

    private fun handleAudioVideoMessage(message: Message): Message {
        return try {
            val filePath = if (!message.mediaData.isNullOrEmpty()) {
                MediaUtils.decodeBase64ToFile(
                    message.mediaData,
                    message.messageType!!,
                    appContext
                )?.absolutePath ?: ""
            } else {
                Log.w("MessageDecode", "Empty mediaData for audio/video")
                ""
            }
            message.copy(mediaData = filePath)
        } catch (e: Exception) {
            Log.e("MessageDecode", "Media processing error", e)
            message.copy(mediaData = "[Media error]")
        }
    }

    private fun handleImageMessage(message: Message): Message {
        return if (!message.mediaData.isNullOrEmpty()) {
            message // Return as-is for lazy loading
        } else {
            Log.w("MessageDecode", "Empty mediaData for image")
            message.copy(mediaData = "")
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
            text = messageText,
            mediaData = encodedMessage,
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
                val chatMetadata = mapOf(
                    "chatId" to chatId,
                    "userId" to userId,
                    "dealerId" to senderId,
                    "name" to senderName,
                    "carId" to carId,
                    "timestamp" to System.currentTimeMillis(),
                )

                firestore.collection("chats")
                    .document(chatId)
                    .set(chatMetadata, SetOptions.merge())
            }
            .addOnFailureListener {
                Log.e("ChatViewModel", "Failed to send message", it)
            }
    }

    fun sendMediaMessage(
        chatId: String,
        base64Media: String,
        type: String,
        senderId: String,
        senderName: String,
        carId: String,
    ) {
        val message = Message(
            mediaData = base64Media,
            messageType = type,
            senderId = senderId,
            name = senderName,
            timestamp = System.currentTimeMillis(),
            id = System.currentTimeMillis().toString(),
            carId = carId,
            notificationSent = false
        )

        viewModelScope.launch {
            try {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .add(message)

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to send message")
            }
        }
    }



    private var dealerMessagesListener: ListenerRegistration? = null

    private val _currentChatId = MutableLiveData<String?>()
    val currentChatId: LiveData<String?> = _currentChatId


    fun observeMessages(chatId: String, context: Context) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val messagesRef = FirebaseFirestore.getInstance()
            .collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)

        val listener = messagesRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            for (doc in snapshot.documents) {
                val message = doc.toObject(Message::class.java) ?: continue

                if (message.senderId != currentUserId && !message.notificationSent) {
                    MessageNotificationUtil.createNotification(context, message)

                    doc.reference.update("notificationSent", true)
                }
            }
        }
        // Store the listener for future removal
        dealerMessagesListener?.remove()
        dealerMessagesListener = listener
    }

    fun observeMessagesForUser(chatId: String, context: Context) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val messagesRef = FirebaseFirestore.getInstance()
            .collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)

        val listener = messagesRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            for (doc in snapshot.documents) {
                val message = doc.toObject(Message::class.java) ?: continue

                if (message.senderId != currentUserId && !message.notificationSent) {
                    MessageNotificationUtil.createNotificationForMain(context, message)

                    doc.reference.update("notificationSent", true)
                }
            }
        }
        // Store the listener for future removal
        dealerMessagesListener?.remove()
        dealerMessagesListener = listener
    }

    private fun removeMessagesListener() {
        dealerMessagesListener?.remove()
        messagesListener?.let { listener ->
            messagesListenerRef?.removeEventListener(listener)
        }
        messagesListener = null
        messagesListenerRef = null
    }

    override fun onCleared() {
        super.onCleared()
        removeMessagesListener()
    }
}

sealed class ChatResult {
    object Success : ChatResult()
    data class Error(val message: String) : ChatResult()
    object Loading : ChatResult()
}
