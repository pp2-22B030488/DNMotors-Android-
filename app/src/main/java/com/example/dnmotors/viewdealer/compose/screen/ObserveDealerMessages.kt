package com.example.dnmotors.viewdealer.compose.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.example.dnmotors.utils.MessageNotificationUtil

@Composable
fun ObserveDealerMessages(userId: String, vin: String) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
//        MessageNotificationUtil.observeNewMessages(vin, userId, context)
        onDispose { /* Optional cleanup */ }
    }
}
