package com.example.dnmotors.viewdealer.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.dnmotors.services.MessageService
import com.example.dnmotors.services.MessageWorkScheduler
import com.example.dnmotors.viewdealer.compose.DealerApp
import com.google.firebase.auth.FirebaseAuth

class DealerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel(applicationContext)
        val userId = intent?.getStringExtra("userId")
        val carId = intent?.getStringExtra("carId")
        if (FirebaseAuth.getInstance().currentUser != null) {
            MessageWorkScheduler.scheduleWorker(this)
            MessageWorkScheduler.triggerNow(this)

        }
        if (FirebaseAuth.getInstance().currentUser != null) {
            val serviceIntent = Intent(this, MessageService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        }
        setContent {
            DealerApp(userId to carId)
        }

    }

    private val CHANNEL_ID = "messages_channel"

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "messages_channel",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for new messages from users"
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


}
