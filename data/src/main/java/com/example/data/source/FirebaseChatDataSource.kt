package com.example.data.source

import com.example.data.model.MessageDTO
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class FirebaseChatDataSource @Inject constructor() : ChatDataSource {
    private val database = FirebaseDatabase.getInstance().reference

    override fun getMessages(chatId: String): Flow<List<MessageDTO>> = callbackFlow {
        val messagesRef = database.child("chats").child(chatId).child("messages")

        val listener = messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { it.getValue(MessageDTO::class.java) }
                trySend(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        })
        awaitClose { messagesRef.removeEventListener(listener) }
    }

    override suspend fun sendMessage(chatId: String, message: MessageDTO) {
        database.child("chats").child(chatId).child("messages").push().setValue(message)
    }

    fun getUserChats(userId: String): Flow<List<String>> = callbackFlow {
        val chatsRef = database.child("chats")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatIds = snapshot.children.mapNotNull { it.key }
                    .filter { it.startsWith(userId) }
                trySend(chatIds)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        chatsRef.addValueEventListener(listener)
        awaitClose { chatsRef.removeEventListener(listener) }
    }

}