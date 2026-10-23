package com.zenlink.hydra.feature.chat

import com.zenlink.hydra.core.llama.GenerationEvent
import com.zenlink.hydra.core.llama.StopReason
import com.zenlink.hydra.core.model.MessageStatus

/** Accumulated state of one assistant reply while it streams. */
data class ReplyState(
    val text: String = "",
    val status: MessageStatus = MessageStatus.STREAMING,
    val error: String? = null,
    val finished: Boolean = false,
)

/** Pure fold over generation events. Kept free of Android types so it is unit-testable on the JVM. */
object ReplyAssembler {
    fun apply(state: ReplyState, event: GenerationEvent): ReplyState = when (event) {
        is GenerationEvent.Token -> if (state.finished) state else state.copy(text = state.text + event.text)
        is GenerationEvent.Done -> state.copy(
            status = when (event.reason) {
                StopReason.END, StopReason.LENGTH -> MessageStatus.COMPLETE
                StopReason.CANCELLED -> MessageStatus.STOPPED
            },
            finished = true,
        )
        is GenerationEvent.Failed -> state.copy(
            status = MessageStatus.FAILED,
            error = event.message,
            finished = true,
        )
    }
}
