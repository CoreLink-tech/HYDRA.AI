package com.zenlink.hydra.core.model

enum class MessageRole { USER, ASSISTANT }

enum class MessageStatus { COMPLETE, STREAMING, STOPPED, FAILED }

data class ChatSummary(
    val id: Long,
    val title: String,
    val modelId: String?,
    val updatedAtMs: Long,
    val messageCount: Int,
)

data class ChatMessage(
    val id: Long,
    val chatId: Long,
    val role: MessageRole,
    val content: String,
    val status: MessageStatus,
    val createdAtMs: Long,
)
