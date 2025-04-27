package com.example.dnmotors.services

import android.app.IntentService
import android.content.Intent
import com.example.domain.model.Message
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class MessageService : IntentService("MessageService") {

    override fun onHandleIntent(intent: Intent?) {
        fetchNewMessageFromDatabase { newMessage ->
            if (newMessage != null) {
                val broadcastIntent = Intent("com.example.app.NEW_MESSAGE")
                broadcastIntent.putExtra("new_message", newMessage)
                sendBroadcast(broadcastIntent)
            }
        }
    }

    private fun fetchNewMessageFromDatabase(callback: (Message?) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return callback(null)

        val firestore = FirebaseFirestore.getInstance()

        val query = firestore.collection("chats")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)

        query.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val documents = task.result
                if (documents != null) {
                    val message = documents.first().toObject(Message::class.java)
                    callback(message)
                } else {
                    callback(null)
                }
            } else {

                callback(null)
            }
        }
    }
}
