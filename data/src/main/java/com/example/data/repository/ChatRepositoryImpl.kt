package com.example.data.repository

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.data.model.ChatItemDao
import com.example.data.model.MessageDao
import com.example.domain.model.Car
import com.example.domain.model.ChatItem
import com.example.domain.model.Message
import com.example.domain.model.User
import com.example.domain.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatRepositoryImpl: ChatRepository {
    private lateinit var car: Car

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun loadChatList(isDealer: Boolean): LiveData<List<ChatItem>> {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return MutableLiveData(emptyList())

//        val currentUserId = auth.currentUser?.uid ?: return liveData
        val queryField = if (isDealer) "dealerId" else "userId"
        val liveData = MutableLiveData<List<ChatItem>>()

        firestore.collection("chats")
            .whereEqualTo(queryField, currentUserId)
            .get()
            .addOnSuccessListener { result ->
                val chatItems = mutableListOf<ChatItem>()
                val jobs = mutableListOf<Deferred<Boolean>>()

                CoroutineScope(Dispatchers.IO).launch {
                    for (doc in result.documents) {
                        val carId = doc.getString("carId") ?: continue
                        val userId = doc.getString("userId") ?: continue
                        val dealerId = doc.getString("dealerId") ?: continue
                        val timestamp = doc.getLong("timestamp") ?: 0L
                        val text = doc.getString("text") ?: ""
                        val imageUrl = doc.get("imageUrl") as? List<String> ?: emptyList()
                        val brand = doc.getString("brand") ?: ""

                        val job = async {
                            val car = getCarByVin(carId)
                            val dealer = getUserById(dealerId)
                            val lastMessagePair = getLastMessage(doc.id)
                            val senderName = getLastMessageSenderName(doc.id)

                            val chatItem = ChatItem(
                                carId = carId,
                                userId = userId,
                                dealerId = dealerId,
                                timestamp = timestamp,
                                text = text,
                                name = senderName ?: "Пользователь",
                                dealerName = dealer?.name ?: "Дилер",
                                brand = car?.brand ?: "",
                                model = car?.model ?: "",
                                year = car?.year ?: 0, // безопасный вызов для year

                                imageUrl = car?.imageUrl ?: emptyList(),
                                lastMessage = lastMessagePair.first,
                                messageTime = lastMessagePair.second,
                                chatId = "${carId}_${dealerId}_${userId}"
                            )
                            chatItems.add(chatItem)
                        }

                        jobs.add(job)
                    }

                    jobs.awaitAll()
                    liveData.postValue(chatItems)
                }
            }
            .addOnFailureListener {
                liveData.postValue(emptyList())
            }

        return liveData
    }


    override suspend fun getCarByVin(vin: String): Car? {
        return try {
            val snapshot = firestore.collection("Cars")
                .whereEqualTo("vin", vin)
                .get()
                .await()
            snapshot.documents.firstOrNull()?.toObject(Car::class.java)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Failed to load car by VIN", e)
            null
        }
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
            }
        return liveData
    }
    private suspend fun getLastMessageSenderName(chatId: String): String? {
        return try {
            val snapshot = firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            val lastMessage = snapshot.documents.firstOrNull()?.toObject(Message::class.java)
            lastMessage?.name // предполагается, что в Message есть поле name
        } catch (e: Exception) {
            Log.e("ChatRepositoryImpl", "Failed to get last message sender name", e)
            null
        }
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


    private suspend fun getCarById(carId: String): Car? {
        return try {
            val snapshot = firestore.collection("Cars")
                .document(carId)
                .get()
                .await()
            snapshot.toObject(Car::class.java)
        } catch (e: Exception) {
            null
        }
    }



    private suspend fun getUserById(userId: String): User? {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun getLastMessage(chatId: String): Pair<String, String> {
        return try {
            val snapshot = firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            val lastMessage = snapshot.documents.firstOrNull()
            val text = lastMessage?.getString("text") ?: ""
            val time = lastMessage?.getLong("timestamp")?.let {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it))
            } ?: ""
            Pair(text, time)
        } catch (e: Exception) {
            Pair("", "")
        }
    }
}

