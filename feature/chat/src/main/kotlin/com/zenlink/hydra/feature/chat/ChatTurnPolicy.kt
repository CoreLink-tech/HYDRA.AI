package com.zenlink.hydra.feature.chat

import com.zenlink.hydra.core.model.ModelState

/**
 * Pure policy for starting / stopping a chat turn. Unit-tested on the JVM without Android.
 */
object ChatTurnPolicy {
    /** Reject blank prompts and concurrent generations. */
    fun canSend(prompt: String, generating: Boolean): Boolean =
        prompt.trim().isNotEmpty() && !generating

    fun noticeNoActiveModel(): String =
        "Download and select a model before chatting."

    fun noticeModelNotInstalled(displayName: String): String =
        "“$displayName” is not installed yet. Open Models to download it."

    fun noticeLoadFailed(detail: String?): String =
        detail?.takeIf { it.isNotBlank() } ?: "Could not load the model."

    fun noticeGenerationFailed(detail: String?): String =
        detail?.takeIf { it.isNotBlank() } ?: "Generation failed."

    /** Whether a model row is usable for inference. */
    fun isInstalled(state: ModelState): Boolean = state == ModelState.INSTALLED
}
