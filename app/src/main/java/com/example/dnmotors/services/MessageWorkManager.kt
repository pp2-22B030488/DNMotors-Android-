//package com.example.dnmotors.services
//
//import android.content.Context
//import androidx.work.BackoffPolicy
//import androidx.work.Constraints
//import androidx.work.ExistingPeriodicWorkPolicy
//import androidx.work.NetworkType
//import androidx.work.OneTimeWorkRequestBuilder
//import androidx.work.PeriodicWorkRequest
//import androidx.work.PeriodicWorkRequestBuilder
//import androidx.work.WorkManager
//import com.google.firebase.auth.FirebaseAuth
//import java.util.concurrent.TimeUnit
//
//object MessageWorkManager {
//    private const val UNIQUE_WORK_NAME = "message_check_work"
//    private val constraints = Constraints.Builder()
//        .setRequiredNetworkType(NetworkType.CONNECTED)
//        .build()
//
//    fun schedulePeriodicCheck(context: Context) {
//        cancelWork(context)
//
//        val workRequest = PeriodicWorkRequestBuilder<MessageCheckWorker>(
//            15, TimeUnit.SECONDS, // Minimum interval
//            5, TimeUnit.SECONDS    // Flex interval
//        )
//            .setConstraints(constraints)
//            .setBackoffCriteria(
//                BackoffPolicy.LINEAR,
//                PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS,
//                TimeUnit.MILLISECONDS
//            )
//            .build()
//
//        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
//            UNIQUE_WORK_NAME,
//            ExistingPeriodicWorkPolicy.UPDATE,
//            workRequest
//        )
//    }
//
//    fun cancelWork(context: Context) {
//        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
//    }
//
//    fun scheduleImmediateCheck(context: Context) {
//        val oneTimeRequest = OneTimeWorkRequestBuilder<MessageCheckWorker>()
//            .setConstraints(constraints)
//            .build()
//
//        WorkManager.getInstance(context)
//            .enqueue(oneTimeRequest)
//    }
//}