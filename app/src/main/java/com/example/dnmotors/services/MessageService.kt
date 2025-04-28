package com.example.app.service

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.dnmotors.R
import com.example.domain.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MessageService : IntentService("MessageService") {

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        return START_STICKY
    }

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
                if (!documents.isEmpty) {
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

    private fun startForegroundService() {
        val channelId = "message_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Message Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
//            .setContentTitle("Listening for messages...")
//            .setContentText("Running in background")
//            .setSmallIcon(R.drawable.logo)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }
}
