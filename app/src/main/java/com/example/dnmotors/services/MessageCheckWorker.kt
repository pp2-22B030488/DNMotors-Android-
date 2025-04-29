package com.example.dnmotors.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.dnmotors.R
import com.example.dnmotors.utils.MessageNotificationUtil
import com.example.dnmotors.view.activity.MainActivity
import com.example.domain.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit

class MessageWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser?.uid ?: return Result.success()

        return try {
            val chats = firestore.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .get().await()

            chats.documents.forEach { chatDoc ->
                val chatId = chatDoc.id
                val messagesRef = chatDoc.reference.collection("messages")

                val snapshot = messagesRef
                    .whereEqualTo("notificationSent", false)
                    .whereNotEqualTo("senderId", currentUserId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(1)
                    .get().await()

                for (doc in snapshot.documents) {
                    val message = doc.toObject(Message::class.java) ?: continue
                    MessageNotificationUtil.sendNotification(applicationContext, message)
                    doc.reference.update("notificationSent", true).await()
                }
            }

            Result.success()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.retry()
        }
    }
}

object MessageWorkScheduler {
    private const val UNIQUE_WORK_NAME = "message_check_work"

    private val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun scheduleWorker(context: Context) {
        val request = PeriodicWorkRequestBuilder<MessageWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun triggerNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<MessageWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}