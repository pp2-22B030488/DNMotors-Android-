package com.example.dnmotors.utils

import android.app.Activity
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

    fun createNotification(
        context: Context,
        message: Message,
        targetActivity: Class<out Activity>,
        extras: Intent.() -> Unit = {}
    ) {
        val intent = Intent(context, targetActivity).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            extras()
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "messages_channel"

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("New message from ${message.name}")
            .setContentText(message.text)
            .setSmallIcon(R.drawable.logo)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }


}
