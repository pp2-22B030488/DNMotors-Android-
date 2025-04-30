package com.example.dnmotors.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.dnmotors.R
import com.example.dnmotors.view.activity.MainActivity
import com.example.dnmotors.viewdealer.activity.DealerActivity
import com.example.domain.model.Message

object MessageNotificationUtil {
    const val CHANNEL_ID = "messages_channel"

    fun createNotification(context: Context, message: Message) {
        val intent = Intent(context, DealerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("userId", message.senderId)
            putExtra("carId", message.carId)
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("New message from ${message.name}")
            .setContentText(message.text)
            .setSmallIcon(R.drawable.logo)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }

    fun createNotificationForMain(context: Context, message: Message) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("carId", message.carId)
            putExtra("dealerId", message.senderId)
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "messages_channel")
            .setContentTitle("New message from ${message.name}")
            .setContentText(message.text)
            .setSmallIcon(R.drawable.logo)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }

}
