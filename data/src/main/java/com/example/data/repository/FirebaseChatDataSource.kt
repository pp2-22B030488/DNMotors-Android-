package com.example.data.repository

import com.example.domain.model.Message
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class FirebaseChatDataSource {

    private val database = FirebaseDatabase.getInstance().reference

    fun getChats(userId: String) = callbackFlow {
        val ref = database.child("messages").child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ids = snapshot.children.mapNotNull { it.key }
                trySend(ids)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addListenerForSingleValueEvent(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getMessages(userId: String, carId: String) = callbackFlow {
        val ref = database.child("messages").child(userId).child(carId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(Message::class.java) }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun sendMessage(userId: String, carId: String, message: Message) {
        database.child("messages").child(userId).child(carId).push().setValue(message)
    }
}
