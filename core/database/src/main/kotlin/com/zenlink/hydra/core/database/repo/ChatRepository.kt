package com.zenlink.hydra.core.database.repo

import com.zenlink.hydra.core.database.dao.ChatDao
import com.zenlink.hydra.core.database.dao.MessageDao
import com.zenlink.hydra.core.database.entities.ChatEntity
import com.zenlink.hydra.core.database.entities.MessageEntity
import com.zenlink.hydra.core.database.dao.ChatWithCount
import com.zenlink.hydra.core.model.ChatMessage
import com.zenlink.hydra.core.model.ChatSummary
import com.zenlink.hydra.core.model.MessageRole
import com.zenlink.hydra.core.model.MessageStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Local-only chat storage. Nothing here touches the network. */
class ChatRepository(
    private val chats: ChatDao,
    private val messages: MessageDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun observeChats(): Flow<List<ChatSummary>> =
        chats.observeChats().map { list -> list.map { it.toSummary() } }

    fun observeMessages(chatId: Long): Flow<List<ChatMessage>> =
        messages.observeMessages(chatId).map { list -> list.map { it.toDomain() } }

    suspend fun createChat(modelId: String?, title: String = "New chat"): Long {
        val now = clock()
        return chats.insertChat(
            ChatEntity(title = title, modelId = modelId, createdAtMs = now, updatedAtMs = now),
        )
    }

    suspend fun appendMessage(chatId: Long, role: MessageRole, content: String, status: MessageStatus): Long {
        val now = clock()
        chats.touch(chatId, now)
        return messages.insert(
            MessageEntity(
                chatId = chatId,
                role = role.name,
                content = content,
                status = status.name,
                createdAtMs = now,
            ),
        )
    }

    /** Persists the latest streamed text. Called on a throttle while a reply is generating. */
    suspend fun updateMessage(message: ChatMessage) {
        messages.update(
            MessageEntity(
                id = message.id,
                chatId = message.chatId,
                role = message.role.name,
                content = message.content,
                status = message.status.name,
                createdAtMs = message.createdAtMs,
            ),
        )
    }

    /** Auto-title from the first user message so history is easy to scan. */
    suspend fun autoTitleIfNew(chatId: Long, firstUserMessage: String) {
        val chat = chats.chat(chatId) ?: return
        if (chat.title != "New chat") return
        val clean = firstUserMessage.trim().replace(Regex("\\s+"), " ")
        val title = if (clean.length > 48) clean.take(48).trimEnd() + "..." else clean
        chats.renameChat(chatId, title.ifBlank { "New chat" })
    }

    suspend fun rename(chatId: Long, title: String) = chats.renameChat(chatId, title.trim().ifBlank { "New chat" })

    suspend fun delete(chatId: Long) = chats.deleteChat(chatId)

    /** "Delete all local data" for chats. Model files are removed by the download manager. */
    suspend fun deleteEverything() = chats.deleteAll()

    suspend fun search(query: String): List<ChatSummary> {
        val safe = query.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
        if (safe.isEmpty()) return emptyList()
        return chats.searchChats(safe).map { it.toSummary() }
    }

    suspend fun messagesFor(chatId: Long): List<ChatMessage> =
        messages.messagesFor(chatId).map { it.toDomain() }

    suspend fun messagesByIds(ids: List<Long>): List<ChatMessage> =
        messages.messagesByIds(ids).map { it.toDomain() }

    private fun ChatWithCount.toSummary() = ChatSummary(id, title, modelId, updatedAtMs, messageCount)

    private fun MessageEntity.toDomain() = ChatMessage(
        id = id,
        chatId = chatId,
        role = MessageRole.valueOf(role),
        content = content,
        status = MessageStatus.valueOf(status),
        createdAtMs = createdAtMs,
    )
}
