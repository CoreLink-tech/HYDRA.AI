package com.zenlink.hydra.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zenlink.hydra.core.database.entities.ChatEntity
import com.zenlink.hydra.core.database.entities.MessageEntity
import com.zenlink.hydra.core.database.entities.ModelEntity
import com.zenlink.hydra.core.database.entities.SettingEntity
import com.zenlink.hydra.core.database.entities.ShareEntity
import kotlinx.coroutines.flow.Flow

data class ChatWithCount(
    val id: Long,
    val title: String,
    val modelId: String?,
    val updatedAtMs: Long,
    val messageCount: Int,
)

@Dao
interface ChatDao {
    @Query(
        """
        SELECT c.id, c.title, c.modelId, c.updatedAtMs,
               (SELECT COUNT(*) FROM messages m WHERE m.chatId = c.id) AS messageCount
        FROM chats c
        WHERE c.archived = 0
        ORDER BY c.updatedAtMs DESC
        """,
    )
    fun observeChats(): Flow<List<ChatWithCount>>

    @Query("SELECT * FROM chats WHERE id = :id")
    suspend fun chat(id: Long): ChatEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertChat(chat: ChatEntity): Long

    @Update
    suspend fun updateChat(chat: ChatEntity)

    @Query("UPDATE chats SET title = :title WHERE id = :id")
    suspend fun renameChat(id: Long, title: String)

    @Query("UPDATE chats SET updatedAtMs = :updatedAtMs WHERE id = :id")
    suspend fun touch(id: Long, updatedAtMs: Long)

    @Query("DELETE FROM chats WHERE id = :id")
    suspend fun deleteChat(id: Long)

    @Query("DELETE FROM chats")
    suspend fun deleteAll()

    @Query(
        """
        SELECT DISTINCT c.id, c.title, c.modelId, c.updatedAtMs,
               (SELECT COUNT(*) FROM messages mm WHERE mm.chatId = c.id) AS messageCount
        FROM chats c
        JOIN messages m ON m.chatId = c.id
        WHERE c.archived = 0 AND (c.title LIKE '%' || :query || '%' ESCAPE '\' OR m.content LIKE '%' || :query || '%' ESCAPE '\')
        ORDER BY c.updatedAtMs DESC
        LIMIT 100
        """,
    )
    suspend fun searchChats(query: String): List<ChatWithCount>
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAtMs ASC, id ASC")
    fun observeMessages(chatId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAtMs ASC, id ASC")
    suspend fun messagesFor(chatId: Long): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE id IN (:ids) ORDER BY createdAtMs ASC")
    suspend fun messagesByIds(ids: List<Long>): List<MessageEntity>

    @Insert
    suspend fun insert(message: MessageEntity): Long

    @Update
    suspend fun update(message: MessageEntity)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface ModelDao {
    @Query("SELECT * FROM models")
    fun observeAll(): Flow<List<ModelEntity>>

    @Query("SELECT * FROM models WHERE modelId = :modelId")
    suspend fun find(modelId: String): ModelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(model: ModelEntity)

    @Delete
    suspend fun delete(model: ModelEntity)
}

@Dao
interface SettingDao {
    @Query("SELECT * FROM settings")
    fun observeAll(): Flow<List<SettingEntity>>

    @Query("SELECT value FROM settings WHERE `key` = :key")
    suspend fun get(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(setting: SettingEntity)
}

@Dao
interface ShareDao {
    @Query("SELECT * FROM shares WHERE chatId = :chatId ORDER BY createdAtMs DESC")
    fun observeForChat(chatId: Long): Flow<List<ShareEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(share: ShareEntity)

    @Query("UPDATE shares SET revokedAtMs = :revokedAtMs WHERE id = :id")
    suspend fun revoke(id: String, revokedAtMs: Long)
}
