//package com.example.data.repository
//
//import android.util.Log
//import com.example.domain.model.Chat
//import com.example.domain.model.Message
//import com.example.domain.usecase.ChatUseCase
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.Query
//import com.google.firebase.firestore.SetOptions
//import kotlinx.coroutines.tasks.await
//
//class ChatRepositoryImpl(
//    private val firestore: FirebaseFirestore
//) : ChatUseCase {
//    override suspend fun sendMessage(chatId: String, message: Message) {
//        val parts = chatId.split("_")
//        if (parts.size != 2) {
//            Log.e("ChatRepo", "Invalid chatId format: $chatId")
//            return
//        }
//        val userId = parts[0]
//        val carId = parts[1]
//
//        val chatRef = firestore.collection("chats").document(chatId)
//        val messageRef = chatRef.collection("messages").document()
//
//        firestore.runTransaction { transaction ->
//            transaction.set(messageRef, message)
//
//            val chatData = mapOf(
//                "lastMessage" to message.text,
//                "timestamp" to message.timestamp,
//                "userId" to userId,
//                "carId" to carId,
//                "participants" to listOf(userId, carId)
//                // Add "dealerId" later if needed, and update participants
//            )
//            transaction.set(chatRef, chatData, SetOptions.merge())
//        }.await()
//    }
//
//    // Inside ChatRepositoryImpl
//    override suspend fun getUserChats(userId: String): List<Chat> {
//        return firestore.collection("chats")
//            .whereArrayContains("participants", userId)
//            // Or query using the dedicated userId field (less flexible if dealers also need to fetch chats):
//            // .whereEqualTo("userId", userId)
//            .orderBy("timestamp", Query.Direction.DESCENDING)
//            .get()
//            .await()
//            .map { doc ->
//                Chat(
//                    chatId = doc.id,
//                    carId = doc.getString("carId") ?: "",
//                    userId = doc.getString("userId") ?: "",
//                    lastMessage = doc.getString("lastMessage") ?: "",
//                    timestamp = doc.getLong("timestamp") ?: 0L
//                )
//            }
//    }
//
//    override fun listenToMessages(chatId: String, onUpdate: (List<Message>) -> Unit) {
//        firestore.collection("chats")
//            .document(chatId)
//            .collection("messages")
//            .orderBy("timestamp")
//            .addSnapshotListener { snapshot, _ ->
//                if (snapshot != null) {
//                    val messages = snapshot.documents.mapNotNull { doc ->
//                        doc.toObject(Message::class.java)
//                    }
//                    onUpdate(messages)
//                }
//            }
//    }
//}
