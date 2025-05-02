package com.example.data.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.lifecycle.LiveData
import com.example.domain.model.ChatItem

@Dao
interface ChatItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChatItems(chatItems: List<ChatItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChatItem(chatItem: ChatItem)

    @Query("SELECT * FROM chat_items WHERE userId = :currentUserId OR dealerId = :currentUserId")
    fun getChatList(currentUserId: String): LiveData<List<ChatItem>>

    @Query("SELECT * FROM chat_items WHERE chatId = :chatId LIMIT 1")
    fun getChatItemById(chatId: String): ChatItem?

    @Query("DELETE FROM chat_items")
    fun deleteAllChatItems()
}