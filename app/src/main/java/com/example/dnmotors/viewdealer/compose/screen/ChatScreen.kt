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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.dnmotors.view.fragments.messagesFragment.ChatsFragment
import com.example.dnmotors.viewmodel.ChatViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.ChatItem
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel(),
    onChatClick: (ChatItem) -> Unit
) {
    val chatItems by viewModel.chatItems.observeAsState(emptyList())
    val dealerId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(dealerId) {
        if (dealerId != null) {
            viewModel.loadChatListForDealer()
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
                    headlineContent = { Text("User ID: ${item.userId}") },
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