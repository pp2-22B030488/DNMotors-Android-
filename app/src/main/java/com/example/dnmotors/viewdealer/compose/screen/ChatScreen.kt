package com.example.dnmotors.viewdealer.compose.screen

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.dnmotors.viewmodel.AuthViewModel
import com.example.dnmotors.viewmodel.ChatViewModel
import com.example.domain.model.ChatItem

@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel,
    authViewModel: AuthViewModel,
    onChatClick: (ChatItem) -> Unit
) {
    val chatItems by chatViewModel.chatItems.observeAsState(emptyList())
    var dealerId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(dealerId) {
        val auth = authViewModel.returnAuth()
        dealerId = auth.uid
        if (dealerId != null) {
            chatViewModel.loadChatList(true)
        }
    }

    if (chatItems.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active chats.")
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(chatItems) { item ->
                ListItem(
                    headlineContent = { Text("User name: ${item.name}") },
                    supportingContent = { Text("Car VIN: ${item.carId}") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onChatClick(item) }
                )
                Divider()
            }
        }
    }
}