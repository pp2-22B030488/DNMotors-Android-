package com.example.dnmotors.viewdealer.compose

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.dnmotors.viewmodel.ChatViewModel

@Composable
fun DealerApp() {
    val navController = rememberNavController()
    val showBottomBar = rememberSaveable { mutableStateOf(true) }
    val context = LocalContext.current
    val chatViewModel: ChatViewModel = viewModel()

    LaunchedEffect(Unit) {
        chatViewModel.chatItems.observeForever { chats ->
            chats.forEach { chat ->
                chatViewModel.observeMessages(chatId = "${chat.dealerId}_${chat.userId}", context)
            }
        }
        chatViewModel.loadChatListForDealer()
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar.value) {
                BottomNavigationBar(navController)
            }
        }
    ) { padding ->
        DealerNavGraph(navController = navController, padding = padding, onToggleBottomBar = { showBottomBar.value = it }
        )
    }
}
