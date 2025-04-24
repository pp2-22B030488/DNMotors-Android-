package com.example.dnmotors.viewmodel

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnmotors.R
import com.example.dnmotors.utils.MessageNotificationUtil.observeNewMessages
import com.example.dnmotors.view.activity.MainActivity
import com.example.dnmotors.view.fragments.messagesFragment.ChatsFragment
import com.example.dnmotors.viewdealer.repository.CarRepository
import com.example.domain.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val _chatItems = MutableLiveData<List<ChatsFragment.ChatItem>>()
    val chatItems: LiveData<List<ChatsFragment.ChatItem>> = _chatItems

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val baseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("messages")

    private var messagesListener: ValueEventListener? = null
    private var messagesListenerRef: DatabaseReference? = null


    fun loadChatListForDealer() {
        val dealerId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewModelScope.launch {
            val cars = CarRepository().getCarsForDealer(dealerId)
            val vins = cars.mapNotNull { it.vin }

            val chatItems = mutableListOf<ChatsFragment.ChatItem>()
            val messagesRef = FirebaseDatabase.getInstance().getReference("messages")
            val remaining = vins.toMutableSet()

            vins.forEach { vin ->
                messagesRef.child(vin).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach { userSnapshot ->
                            val userId = userSnapshot.key ?: return@forEach
                            chatItems.add(ChatsFragment.ChatItem(vin = vin, userId = userId))
                        }

                        remaining.remove(vin)
                        if (remaining.isEmpty()) {
                            _chatItems.value = chatItems
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println("Error reading messages for VIN $vin: ${error.message}")
                        remaining.remove(vin)
                        if (remaining.isEmpty()) {
                            _chatItems.value = chatItems
                        }
                    }
                })
            }

            if (vins.isEmpty()) {
                _chatItems.value = emptyList()
            }
        }
    }

    fun loadMessages(carId: String, userId: String) {
        removeMessagesListener()

        val messagesRef = baseRef.child(carId).child(userId)
        messagesListenerRef = messagesRef

        messagesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messageList = snapshot.children.mapNotNull { data ->
                    val msg = data.getValue(Message::class.java)
                    msg?.copy(
                        message = try {
                            val decodedBytes = Base64.decode(msg.message, Base64.DEFAULT)
                            String(decodedBytes, Charsets.UTF_8)
                        } catch (e: Exception) {
                            "[Error decoding]"
                        }
                    )
                }.sortedBy { it.timestamp }

                _messages.value = messageList
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error loading messages: ${error.message}")
                _messages.value = emptyList()
            }
        }

        messagesRef.addValueEventListener(messagesListener!!)
    }

    fun sendMessage(
        carId: String,
        userId: String,
        messageText: String,
        dealerId: String,
        dealerName: String
    ) {
        if (messageText.isBlank()) return

        val messagesRef = baseRef.child(carId).child(userId)
        val messageId = messagesRef.push().key ?: System.currentTimeMillis().toString()

        // ✅ Encode message to Base64
        val encodedMessage = Base64.encodeToString(messageText.toByteArray(Charsets.UTF_8), Base64.DEFAULT)

        val message = Message(
            id = messageId,
            senderId = dealerId,
            name = dealerName,
            message = encodedMessage,
            messageType = "text",
            timestamp = System.currentTimeMillis()
        )

        messagesRef.child(messageId).setValue(message)
    }


    private fun removeMessagesListener() {
        messagesListener?.let { listener ->
            messagesListenerRef?.removeEventListener(listener)
        }
        messagesListener = null
        messagesListenerRef = null
    }
    fun observeMessages(
        vin: String,
        userId: String,
        context: Context
    ) {
        observeNewMessages(vin, userId, context) { newMessage ->
            // Update LiveData
            val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()
            currentMessages.add(newMessage)
            _messages.postValue(currentMessages)

            // Show notification when a new message is received
            showNotification(context, newMessage)
        }
    }

    private fun showNotification(context: Context, message: Message) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(context, "messages_channel")
            .setSmallIcon(R.drawable.ic_settings)
            .setContentTitle("New message from ${message.name}")
            .setContentText(message.message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = NotificationManagerCompat.from(context)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
        }
    }


    override fun onCleared() {
        super.onCleared()
        removeMessagesListener()
    }
}