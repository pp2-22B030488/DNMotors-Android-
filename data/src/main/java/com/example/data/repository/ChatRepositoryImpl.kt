package com.example.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.domain.model.ChatItem
import com.example.domain.model.Message
import com.example.domain.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatRepositoryImpl : ChatRepository {
    private val firestore = FirebaseFirestore.getInstance()

    override fun loadChatList(isDealer: Boolean): LiveData<List<ChatItem>> {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return MutableLiveData(emptyList())

        val queryField = if (isDealer) "dealerId" else "userId"

        val liveData = MutableLiveData<List<ChatItem>>()

        firestore.collection("chats")
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
                liveData.postValue(chatItems)
            }
            .addOnFailureListener { error ->
                Log.e("ChatRepositoryImpl", "Error loading chat list: ${error.message}")
                liveData.postValue(emptyList())
            }

        return liveData
    }

    override fun loadMessages(chatId: String): LiveData<List<Message>> {
        val liveData = MutableLiveData<List<Message>>()

        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatRepositoryImpl", "Error loading messages", error)
                    liveData.postValue(emptyList())
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)
                } ?: emptyList()

                liveData.postValue(messages)
            }

        return liveData
    }

    override fun sendMessage(message: Message, chatId: String) {
        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                // Additional logic to update chat metadata
            }
            .addOnFailureListener {
                Log.e("ChatRepositoryImpl", "Failed to send message", it)
            }
    }

    override fun sendMediaMessage(message: Message, chatId: String) {
        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(message)
            .addOnFailureListener {
                Log.e("ChatRepositoryImpl", "Failed to send media message", it)
            }
    }

    override fun observeMessages(chatId: String, context: Context) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val listener = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                snapshot.documents.forEach { doc ->
                    val message = doc.toObject(Message::class.java) ?: return@forEach
                    if (message.senderId != currentUserId && !message.notificationSent) {
                        doc.reference.update("notificationSent", true)
                    }
                }
            }
    }

    override fun observeNewMessages(chatId: String): LiveData<Message> {
        val result = MutableLiveData<Message>()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return result

        FirebaseFirestore.getInstance()
            .collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                for (doc in snapshot.documents) {
                    val message = doc.toObject(Message::class.java) ?: continue
                    if (message.senderId != currentUserId && !message.notificationSent) {
                        result.postValue(message)
                        doc.reference.update("notificationSent", true)
                    }
                }
            }

        return result
    }

}

