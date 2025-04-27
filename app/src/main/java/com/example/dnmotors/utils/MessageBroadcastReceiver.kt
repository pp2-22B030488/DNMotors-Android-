package com.example.dnmotors.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.domain.model.Message

class MessageBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getParcelableExtra<Message>("new_message") ?: return
        MessageNotificationUtil.sendNotification(context, message)
    }
}
