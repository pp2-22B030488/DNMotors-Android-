package com.example.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.data.model.ChatItemDao
import com.example.data.model.MessageDao
import com.example.domain.model.ChatItem
import com.example.domain.model.Message
import com.example.domain.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatRepositoryImpl
//    (
//    private val chatItemDao: ChatItemDao,
//    private val messageDao: MessageDao,
//    private val applicationScope: CoroutineScope
//)
    : ChatRepository {
    private val firestore = FirebaseFirestore.getInstance()

    override fun loadChatList(isDealer: Boolean): LiveData<List<ChatItem>> {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return MutableLiveData(emptyList())
//        val roomLiveData = chatItemDao.getChatList(currentUserId)

        val queryField = if (isDealer) "dealerId" else "userId"

        val liveData = MutableLiveData<List<ChatItem>>()

        firestore.collection("chats")
            .whereEqualTo(queryField, currentUserId)
            .get()
            .addOnSuccessListener { result ->
                val chatItems = result.documents.mapNotNull { doc ->
//                    val chatId = doc.id
                    val carId = doc.getString("carId")
                    val fetcheduserId = doc.getString("userId")
                    val fetchedDealerId = doc.getString("dealerId")
                    val name = doc.getString("name")
                    val timestamp = doc.getLong("timestamp")

                    if (carId != null && fetcheduserId != null && name != null && timestamp != null && fetchedDealerId !=null) {
                        ChatItem(
//                            chatId = chatId,
                            carId = carId,
                            userId = fetcheduserId,
                            dealerId = fetchedDealerId,
                            timestamp = timestamp,
                            name = name
                        )
                    } else null
                }
                liveData.postValue(chatItems)

//                chatItemDao.deleteAllChatItems()
//                chatItemDao.insertChatItems(chatItems)
            }
            .addOnFailureListener { error ->
                Log.e("ChatRepositoryImpl", "Error loading chat list: ${error.message}")
                liveData.postValue(emptyList())
            }
        return liveData

//        return roomLiveData
    }

    override fun loadMessages(chatId: String): LiveData<List<Message>> {
//        val roomLiveData = messageDao.getMessagesForChat(chatId)
        val liveData = MutableLiveData<List<Message>>()

        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatRepositoryImpl", "Error loading messages", error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)
                } ?: emptyList()

                liveData.postValue(messages)

//                if (snapshot == null) return@addSnapshotListener
//
//                applicationScope.launch(Dispatchers.IO) {
//                    for (dc in snapshot.documentChanges) {
//                        val message = dc.document.toObject(Message::class.java)
//                            .copy(chatId = chatId)
//
//                        when (dc.type) {
//                            DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
//                                messageDao.insertMessage(message)
//                            }
//
//                            DocumentChange.Type.REMOVED -> {
//                                message.id?.let { messageDao.deleteMessageById(it) }
//                            }
//                        }
//                    }
//                }
            }
        return liveData

//        return roomLiveData
    }

    override fun sendMessage(
        message: Message,
        chatId: String) {
        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                val chatMetadata = mapOf(
                    "userId" to message.userId,
                    "dealerId" to message.dealerId,
                    "name" to message.text,
                    "carId" to message.carId,
                    "timestamp" to System.currentTimeMillis(),
                )

                firestore.collection("chats")
                    .document(chatId)
                    .set(chatMetadata, SetOptions.merge())
            }
            .addOnFailureListener {
                Log.e("ChatRepositoryImpl", "Failed to send message", it)
            }
//        val messageWithChatId = message.copy(chatId = chatId)
//        applicationScope.launch(Dispatchers.IO) {
//
//            val newMessageRef = firestore.collection("chats").document(chatId).collection("messages").document()
//            val messageToSave = messageWithChatId.copy(id = newMessageRef.id)
//
//            messageDao.insertMessage(messageToSave)
//
//            newMessageRef.set(messageToSave)
//                .addOnSuccessListener {
//                    Log.d("ChatRepositoryImpl", "Message sent successfully to Firestore")
//                }
//                .addOnFailureListener { e ->
//                    Log.e("ChatRepositoryImpl", "Failed to send message to Firestore", e)
//                }
//        }
    }

    override fun sendMediaMessage(message: Message, chatId: String) {
        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(message)
            .addOnFailureListener {
                Log.e("ChatRepositoryImpl", "Failed to send media message", it)
            }
//        val messageWithChatId = message.copy(chatId = chatId)
//
//        applicationScope.launch(Dispatchers.IO) {
//            val newMessageRef = firestore.collection("chats").document(chatId).collection("messages").document()
//            val messageToSave = messageWithChatId.copy(id = newMessageRef.id)
//
//            messageDao.insertMessage(messageToSave)
//
//            newMessageRef.set(messageToSave)
//                .addOnSuccessListener {
//                    Log.d("ChatRepositoryImpl", "Media message sent successfully to Firestore")
//                }
//                .addOnFailureListener { e ->
//                    Log.e("ChatRepositoryImpl", "Failed to send media message to Firestore", e)
//                }
//        }
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

