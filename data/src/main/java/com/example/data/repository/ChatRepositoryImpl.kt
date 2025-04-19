//package com.example.data.repository
//
//import com.example.domain.model.Message
//import com.example.domain.repository.ChatRepository
//import kotlinx.coroutines.flow.Flow
//
//class ChatRepositoryImpl(
//    private val firebaseSource: FirebaseChatDataSource
//) : ChatRepository {
//
//    override fun getChats(userId: String): Flow<List<String>> {
//        return firebaseSource.getChats(userId)
//    }
//
//    override fun getMessages(userId: String, carId: String): Flow<List<Message>> {
//        return firebaseSource.getMessages(userId, carId)
//    }
//
//    override suspend fun sendMessage(userId: String, carId: String, message: Message) {
//        firebaseSource.sendMessage(userId, carId, message)
//    }
//}
