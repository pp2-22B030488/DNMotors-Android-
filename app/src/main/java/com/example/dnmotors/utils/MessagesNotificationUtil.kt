package com.example.dnmotors.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.dnmotors.R
import com.example.dnmotors.view.activity.MainActivity
import com.example.domain.model.Message

object MessageNotificationUtil {

    fun sendNotification(context: Context, message: Message) {
        if (message.notificationSent) {
            return
        }

        val channelId = "messages_channel"
        val notificationManager = NotificationManagerCompat.from(context)

        // Check if the notification permission is granted
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        // Create the notification channel if not already created
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Messages", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new chat messages"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent to open MainActivity with necessary data (chatId)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("chatId", message.carId)
            putExtra("senderId", message.senderId)
        }

        // Create a pending intent to open MainActivity when the notification is tapped
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_settings)
            .setContentTitle("New message from ${message.name}")
            .setContentText(message.text ?: "You have a new message")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)  // Dismiss notification after tapping
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // Send the notification
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

}
