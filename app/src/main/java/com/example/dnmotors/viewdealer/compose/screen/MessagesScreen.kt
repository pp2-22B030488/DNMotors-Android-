package com.example.dnmotors.viewdealer.compose.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.dnmotors.viewmodel.ChatViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dnmotors.viewmodel.MainViewModel

@Composable
fun MessagesScreen(
    vin: String,
    userId: String,
    dealerId: String,
    dealerName: String,
    viewModel: ChatViewModel = viewModel(),
    onToggleBottomBar: (Boolean) -> Unit

) {
    val messages by viewModel.messages.observeAsState(emptyList())
    var input by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(vin, userId) {
        viewModel.loadMessages(vin, userId)
        viewModel.observeMessages(vin, userId, context)
    }

    DisposableEffect(Unit) {
        onToggleBottomBar(false)
        onDispose {
            onToggleBottomBar(true)
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp) // Padding the whole screen
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .height(300.dp) // debug only
                .fillMaxWidth()
        ) {
            items(messages) { msg ->
                Text(
                    text = "${msg.name}: ${msg.message}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (input.isNotBlank()) {
                        viewModel.sendMessage(vin, userId, input.trim(), dealerId, dealerName)
                        input = ""
                    }
                }
            ) {
                Text("Send")
            }
        }
    }
}
