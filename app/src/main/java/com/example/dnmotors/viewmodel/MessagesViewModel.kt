//package com.example.dnmotors.viewmodel
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.domain.model.Message
//import com.google.firebase.auth.FirebaseAuth
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//class MessagesViewModel @Inject constructor(
//    private val getMessagesUseCase: GetMessagesUseCase,
//    private val sendMessageUseCase: SendMessageUseCase,
//    private val auth: FirebaseAuth
//) : ViewModel() {
//    private val _messages = MutableStateFlow<List<Message>>(emptyList())
//    val messages = _messages.asStateFlow()
//    private lateinit var chatId: String
//
//    fun initChat(dealerId: String) {
//        chatId = "${auth.currentUser?.uid}_$dealerId"
//        loadMessages()
//    }
//
//    private fun loadMessages() {
//        viewModelScope.launch {
//            getMessagesUseCase(chatId).collect { messages ->
//                _messages.value = messages
//            }
//        }
//    }
//
//    fun sendMessage(text: String) {
//        val message = Message(
//            senderId = auth.currentUser?.uid ?: "",
//            senderName = auth.currentUser?.displayName ?: "User",
//            text = text,
//            timestamp = System.currentTimeMillis()
//        )
//        viewModelScope.launch {
//            sendMessageUseCase(chatId, message)
//        }
//    }
//}