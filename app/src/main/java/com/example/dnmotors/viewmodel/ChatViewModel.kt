//import android.util.Log
//import androidx.lifecycle.*
//import com.example.domain.model.Chat
//import com.example.domain.model.Message
//import com.example.domain.usecase.ChatUseCase
//import com.google.firebase.auth.FirebaseAuth
//import kotlinx.coroutines.launch
//import javax.inject.Inject // Assuming Hilt or Dagger for injection
//
//// import your Chat, Message, ChatUseCase models/interfaces
//
//// Annotate with @HiltViewModel if using Hilt
//class ChatViewModel @Inject constructor( // Use @Inject if using Hilt/Dagger
//    private val chatUseCase: ChatUseCase // Inject the UseCase (backed by ChatRepositoryImpl)
//) : ViewModel() {
//
//    // Make currentUserId observable or easily accessible if needed elsewhere
//    val currentUserId: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""
//
//    private val _chatList = MutableLiveData<List<Chat>>()
//    val chatList: LiveData<List<Chat>> = _chatList
//
//    private val _messageList = MutableLiveData<List<Message>>()
//    val messageList: LiveData<List<Message>> = _messageList
//
//    // Consider adding state for loading/errors
//    private val _isLoadingChats = MutableLiveData<Boolean>()
//    val isLoadingChats: LiveData<Boolean> = _isLoadingChats
//
//    private val _isLoadingMessages = MutableLiveData<Boolean>()
//    val isLoadingMessages: LiveData<Boolean> = _isLoadingMessages
//
//    private val _error = MutableLiveData<String?>()
//    val error: LiveData<String?> = _error
//
//
//    // Listener registration for messages, needs to be cleared
//    private var messageListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
//
//
//    fun listenToMessages(chatId: String) {
//        if (chatId.isEmpty()) {
//            _error.postValue("Cannot load messages: Chat ID is missing.")
//            return
//        }
//        if (currentUserId.isEmpty()){
//            _error.postValue("Cannot load messages: User not logged in.")
//            return
//        }
//        // Remove previous listener if any
//        messageListenerRegistration?.remove()
//        _isLoadingMessages.postValue(true)
//
//        // Call the use case method which now internally uses addSnapshotListener
//        // The use case itself doesn't return the registration, so we rely on the callback.
//        // For more robust lifecycle mgmt, the use case could return ListenerRegistration
//        chatUseCase.listenToMessages(chatId) { messages ->
//            _messageList.postValue(messages)
//            _isLoadingMessages.postValue(false)
//        }
//        // How to clean up? The repository listener lives on.
//        // A better pattern: have the use case/repo return a Flow<List<Message>>
//        // which can be collected in viewModelScope and automatically cancelled.
//        // Or, modify listenToMessages to return the ListenerRegistration.
//    }
//
//    // Example modification to return ListenerRegistration (requires changing UseCase/Repo)
//    /*
//    fun listenToMessagesWithCleanup(chatId: String) {
//        messageListenerRegistration?.remove() // Clean up old one
//        _isLoadingMessages.postValue(true)
//        // Assume chatUseCase.listenToMessages now returns ListenerRegistration
//        messageListenerRegistration = chatUseCase.listenToMessages(chatId) { messages ->
//            _messageList.postValue(messages)
//            _isLoadingMessages.postValue(false)
//        }
//    }
//
//    override fun onCleared() {
//        super.onCleared()
//        messageListenerRegistration?.remove() // Clean up listener when ViewModel is destroyed
//    }
//    */
//
//
//    fun sendMessage(chatId: String, text: String) {
//        if (currentUserId.isEmpty()){
//            _error.postValue("Cannot send message: User not logged in.")
//            return
//        }
//        if (chatId.isEmpty()){
//            _error.postValue("Cannot send message: Chat ID is missing.")
//            return
//        }
//        val trimmedText = text.trim()
//        if (trimmedText.isEmpty()) {
//            return // Don't send empty messages
//        }
//
//        val message = Message(
//            senderId = currentUserId, // Use the actual logged-in user's ID
//            text = trimmedText,
//            timestamp = System.currentTimeMillis()
//        )
//
//        viewModelScope.launch {
//            try {
//                chatUseCase.sendMessage(chatId, message)
//                // Optionally clear error state on success
//                _error.postValue(null)
//            } catch (e: Exception) {
//                Log.e("ChatViewModel", "Error sending message for chatId $chatId", e)
//                _error.postValue("Failed to send message. Please try again.")
//            }
//        }
//    }
//
//    fun loadChats() {
//        if (currentUserId.isEmpty()){
//            _error.postValue("Cannot load chats: User not logged in.")
//            _chatList.postValue(emptyList()) // Clear list if user logs out
//            return
//        }
//        _isLoadingChats.postValue(true)
//        _error.postValue(null) // Clear previous errors
//        viewModelScope.launch {
//            try {
//                val chats = chatUseCase.getUserChats(currentUserId)
//                _chatList.postValue(chats)
//            } catch (e: Exception) {
//                Log.e("ChatViewModel", "Error loading chats for user $currentUserId", e)
//                _error.postValue("Failed to load chats.")
//                _chatList.postValue(emptyList()) // Set empty list on error
//            } finally {
//                _isLoadingChats.postValue(false)
//            }
//        }
//    }
//
//    // Call this when the user logs out to clear data
//    fun clearChatData() {
//        _chatList.postValue(emptyList())
//        _messageList.postValue(emptyList())
//        messageListenerRegistration?.remove()
//        messageListenerRegistration = null
//    }
//
//    // Call this in Fragment's onDestroyView or ViewModel's onCleared if needed
//    fun stopListeningToMessages() {
//        messageListenerRegistration?.remove()
//        messageListenerRegistration = null
//        _messageList.postValue(emptyList()) // Clear messages when not listening
//    }
//}