package com.example.dnmotors.services

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.dnmotors.App.Companion.context
import com.example.dnmotors.R
import com.example.dnmotors.utils.MessageNotificationUtil
import com.example.dnmotors.view.activity.MainActivity
import com.example.domain.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class MessageService : IntentService("MessageService") {
    private var listener: ListenerRegistration? = null
    private val channelId = "messages_channel"

    @Deprecated("Deprecated in Java")
    override fun onCreate() {
        super.onCreate()
        startForegroundService()

    }

    @Deprecated("Deprecated in Java")
    override fun onHandleIntent(intent: Intent?) {}


    private fun startForegroundService() {

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Message Service")
            .setContentText("Listening for new messages")
            .setSmallIcon(R.drawable.logo)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    @Deprecated("Deprecated in Java")
    override fun onDestroy() {
        listener?.remove()
        super.onDestroy()
    }
}