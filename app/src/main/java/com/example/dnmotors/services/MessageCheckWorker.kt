package com.example.dnmotors.services

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
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
            val chatSnapshots = firestore.collection("chats")
                .whereEqualTo("userId", currentUserId)
                .get()
                .await()

            for (chatDoc in chatSnapshots.documents) {
                val carId = chatDoc.getString("carId") ?: continue
                val dealerId = chatDoc.getString("dealerId") ?: continue
                val userId = chatDoc.getString("userId") ?: continue

                val chatId = "${carId}_${dealerId}_${userId}" // <--- Ключевой момент

                val messagesRef = firestore.collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(1)

                val messageSnapshots = messagesRef
                    .whereEqualTo("notificationSent", false)
                    .whereNotEqualTo("senderId", currentUserId)
                    .get()
                    .await()

                for (doc in messageSnapshots.documents) {
                    val message = doc.toObject(Message::class.java) ?: continue
                    MessageNotificationUtil.createNotification(
                        applicationContext,
                        message,
                        targetActivity = MainActivity::class.java
                    ) {
                        putExtra("carId", message.carId)
                        putExtra("dealerId", message.dealerId)
                    }
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