package com.example.dnmotors.viewdealer.compose.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.dnmotors.viewmodel.ChatViewModel
import com.example.dnmotors.viewmodel.AuthViewModel
import com.example.domain.model.ChatItem
import com.example.dnmotors.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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

    Column(modifier = Modifier.fillMaxSize()) {
        // AppBar
        TopAppBar(
            title = { Text("Chats") },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            modifier = Modifier.shadow(4.dp)
        )

        // Chat List
        if (chatItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No active chats.")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(chatItems) { item ->
                    ChatItemView(item = item, onClick = onChatClick)
                }
            }
        }
    }
}

@Composable
fun ChatItemView(item: ChatItem, onClick: (ChatItem) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick(item) }
            .shadow(4.dp, shape = MaterialTheme.shapes.medium),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            // Car image — строго 80x80dp
            if (item.imageUrl.isNotEmpty() && item.imageUrl.first().isNotBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(item.imageUrl.first()),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(MaterialTheme.shapes.small),
                    contentScale = ContentScale.Crop
                )
            }
            else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.Gray, shape = MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text("?", color = Color.White)
                }
            }
            // Info column
            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                // Dealer + Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.dealerName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                    Text(
                        text = formatTimestamp(item.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black
                    )
                }

                // Car Name
                Text(
                    text = "${item.brand} ${item.model} ${item.year}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Last message
                Text(
                    text = item.lastMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Функция для форматирования даты, как в RecyclerView адаптере
fun formatTimestamp(timestamp: Long): String {
    val messageDate = Calendar.getInstance().apply {
        timeInMillis = timestamp
    }
    val now = Calendar.getInstance()

    val isToday = now.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR)
            && now.get(Calendar.DAY_OF_YEAR) == messageDate.get(Calendar.DAY_OF_YEAR)

    val isYesterday = now.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR)
            && now.get(Calendar.DAY_OF_YEAR) - messageDate.get(Calendar.DAY_OF_YEAR) == 1

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    return when {
        isToday -> "Сегодня, ${timeFormat.format(Date(timestamp))}"
        isYesterday -> "Вчера, ${timeFormat.format(Date(timestamp))}"
        else -> SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
}
