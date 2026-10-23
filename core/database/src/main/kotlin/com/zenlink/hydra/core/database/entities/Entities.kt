package com.zenlink.hydra.core.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "chats", indices = [Index("updatedAtMs")])
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val modelId: String?,
    val createdAtMs: Long,
    val updatedAtMs: Long,
    val archived: Boolean = false,
)

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["id"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("chatId"), Index("createdAtMs")],
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: Long,
    /** "USER" or "ASSISTANT" */
    val role: String,
    val content: String,
    /** "COMPLETE", "STREAMING", "STOPPED" or "FAILED" */
    val status: String,
    val createdAtMs: Long,
)

@Entity(tableName = "models")
data class ModelEntity(
    @PrimaryKey val modelId: String,
    val version: Int,
    val state: String,
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val localPath: String?,
    val lastError: String?,
    val updatedAtMs: Long,
)

/** Key-value settings. Typed access lives in SettingsRepository. */
@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey val key: String,
    val value: String,
)

@Entity(tableName = "shares")
data class ShareEntity(
    @PrimaryKey val id: String,
    val chatId: Long,
    val mode: String,
    val transport: String,
    val createdAtMs: Long,
    val revokedAtMs: Long?,
)
