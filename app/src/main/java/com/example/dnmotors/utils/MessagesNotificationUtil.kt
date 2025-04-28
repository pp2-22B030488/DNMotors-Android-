package com.example.dnmotors.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.dnmotors.R
import com.example.dnmotors.view.activity.MainActivity
import com.example.domain.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

object MessageNotificationUtil {

    private val firestore = FirebaseFirestore.getInstance()
    private val listeners = mutableMapOf<String, ListenerRegistration>()

    fun observeNewMessages(
        chatId: String,
        context: Context,
        onNewMessage: (Message) -> Unit = {}
    ) {
        listeners[chatId]?.remove()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        val listener = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val doc = snapshot.documents.firstOrNull()
                val newMessage = doc?.toObject(Message::class.java)

//                val newMessage = snapshot.documents.firstOrNull()?.toObject(Message::class.java)
                newMessage?.let {
                    if (it.senderId == currentUserId) return@addSnapshotListener

                    val decoded = it.copy(
                        text = try {
                            val bytes = Base64.decode(it.text, Base64.DEFAULT)
                            String(bytes, Charsets.UTF_8)
                        } catch (e: Exception) {
                            "[Error decoding]"
                        }
                    )

                    sendNotification(context, decoded)
                    onNewMessage(decoded)
                    doc.reference.update("notificationSent", true)

                }
            }

        listeners[chatId] = listener
    }

    fun removeObserver(chatId: String) {
        listeners.remove(chatId)?.remove()
    }

    fun sendNotification(context: Context, message: Message) {
        if (message.notificationSent) {
            return
        }

        val channelId = "messages_channel"
        val notificationManager = NotificationManagerCompat.from(context)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Messages", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new chat messages"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_settings)
            .setContentTitle("New message from ${message.name}")
            .setContentText(message.text ?: "You have a new message")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

}
