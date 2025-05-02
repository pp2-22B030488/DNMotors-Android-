package com.example.dnmotors.viewdealer.compose

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.dnmotors.viewdealer.activity.DealerActivity
import com.example.dnmotors.viewmodel.AuthViewModel
import com.example.dnmotors.viewmodel.ChatViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun DealerApp(messageData: Pair<String?, String?>?) {
    val navController = rememberNavController()
    val showBottomBar = rememberSaveable { mutableStateOf(true) }
    val context = LocalContext.current

    val chatViewModel: ChatViewModel = koinViewModel()
    val authViewModel: AuthViewModel = koinViewModel()

    LaunchedEffect(Unit) {

        chatViewModel.chatItems.observeForever { chats ->
            chats.forEach { chat ->
                chatViewModel.observeMessages(chatId = "${chat.dealerId}_${chat.userId}", context)
            }
        }
        chatViewModel.loadChatList(true)
        chatViewModel.chatItems.observeForever { chats ->
            chats.forEach { chat ->
                chatViewModel.observeNewMessages(
                    chatId = "${chat.dealerId}_${chat.userId}",
                    context,
                    activityClass = DealerActivity::class.java)
            }
        }
    }
    LaunchedEffect(messageData) {
        messageData?.let { (userId, carId) ->
            if (userId != null && carId != null) {
                navController.navigate("messages/$userId/$carId")
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar.value) {
                BottomNavigationBar(navController)
            }
        }
    ) { padding ->
        DealerNavGraph(
            navController = navController,
            padding = padding,
            onToggleBottomBar = { showBottomBar.value = it },
            chatViewModel = chatViewModel,
            authViewModel = authViewModel
        )
    }

}
