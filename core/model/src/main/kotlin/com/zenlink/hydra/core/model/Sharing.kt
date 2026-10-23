package com.zenlink.hydra.core.model

/** What the sender chooses to share. Nothing beyond the chosen scope is ever sent. */
enum class ShareMode(val label: String, val description: String) {
    FULL("Full chat", "Everything in this conversation up to now"),
    SELECTED("Selected messages", "Only the messages you pick"),
    READ_ONLY("Read-only", "The recipient can read it but cannot continue it"),
    CONTINUABLE("Continue locally", "The recipient can keep chatting from this point with their own model"),
}

enum class ShareTransport { NEARBY, ONLINE }

data class ShareRecord(
    val id: String,
    val chatId: Long,
    val mode: ShareMode,
    val transport: ShareTransport,
    val createdAtMs: Long,
    val revokedAtMs: Long?,
)
