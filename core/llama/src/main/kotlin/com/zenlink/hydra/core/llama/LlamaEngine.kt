package com.zenlink.hydra.core.llama

import com.zenlink.hydra.core.model.ChatMessage
import com.zenlink.hydra.core.model.MessageRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Settings for one load. Derived from the device profile and the chosen performance mode. */
data class LoadConfig(
    val contextTokens: Int = 4096,
    val threads: Int = 4,
    val batchTokens: Int = 512,
)

data class SampleConfig(
    val temperature: Float = 0.7f,
    val topP: Float = 0.9f,
    val topK: Int = 40,
    val maxNewTokens: Int = 768,
    val seed: Int = 0,
)

/** Streamed output from one generation. Exactly one [Done] or [Failed] ends the flow. */
sealed interface GenerationEvent {
    data class Token(val text: String) : GenerationEvent
    data class Done(val reason: StopReason) : GenerationEvent
    data class Failed(val message: String) : GenerationEvent
}

enum class StopReason { END, LENGTH, CANCELLED }

/** Coroutine facade over the native engine. One generation at a time, serialised by a mutex. */
class LlamaEngine(
    private val bridge: NativeGateway = JniGateway,
) {
    private val lock = Mutex()

    @Volatile
    var isLoaded: Boolean = false
        private set

    /** Returns null on success, otherwise a message suitable for showing the user. */
    suspend fun load(path: String, config: LoadConfig = LoadConfig()): String? =
        withContext(Dispatchers.Default) {
            lock.withLock {
                val error = bridge.load(path, config)
                isLoaded = error.isEmpty()
                error.ifEmpty { null }
            }
        }

    suspend fun unload() = withContext(Dispatchers.Default) {
        lock.withLock {
            bridge.release()
            isLoaded = false
        }
    }

    /** Formats chat history with ChatML, the template used by the curated Qwen models. */
    fun buildPrompt(history: List<ChatMessage>, systemPrompt: String = DEFAULT_SYSTEM_PROMPT): String =
        buildString {
            append("<|im_start|>system\n").append(systemPrompt).append("<|im_end|>\n")
            history.forEach { m ->
                val role = if (m.role == MessageRole.USER) "user" else "assistant"
                append("<|im_start|>").append(role).append("\n")
                append(m.content).append("<|im_end|>\n")
            }
            append("<|im_start|>assistant\n")
        }

    /**
     * Streams tokens for [prompt]. If the collector stops early, the native cancel flag is set
     * so generation ends after the current token.
     */
    fun generate(prompt: String, config: SampleConfig = SampleConfig()): Flow<GenerationEvent> =
        channelFlow {
            val worker = launch(Dispatchers.Default) {
                lock.withLock {
                    if (!isLoaded) {
                        trySend(GenerationEvent.Failed("model not loaded"))
                        return@withLock
                    }
                    val reason = bridge.generate(prompt, config) { piece ->
                        trySend(GenerationEvent.Token(piece)).isSuccess
                    }
                    val event = when (reason) {
                        "end" -> GenerationEvent.Done(StopReason.END)
                        "length" -> GenerationEvent.Done(StopReason.LENGTH)
                        "cancelled" -> GenerationEvent.Done(StopReason.CANCELLED)
                        else -> GenerationEvent.Failed(reason)
                    }
                    trySend(event)
                }
            }
            try {
                worker.join()
            } finally {
                if (worker.isActive) bridge.cancel()
            }
        }.buffer(Channel.UNLIMITED)

    companion object {
        const val DEFAULT_SYSTEM_PROMPT =
            "You are Hydra, a private on-device assistant. Answer clearly and briefly."
    }
}

/** Seam for tests: production uses [JniGateway], unit tests use a fake. */
interface NativeGateway {
    fun load(path: String, config: LoadConfig): String
    fun generate(prompt: String, config: SampleConfig, onToken: (String) -> Boolean): String
    fun cancel()
    fun release()
}

internal object JniGateway : NativeGateway {
    override fun load(path: String, config: LoadConfig): String =
        NativeBridge.nativeLoad(path, config.contextTokens, config.threads, config.batchTokens)

    override fun generate(prompt: String, config: SampleConfig, onToken: (String) -> Boolean): String =
        NativeBridge.nativeGenerate(
            prompt, config.temperature, config.topP, config.topK, config.maxNewTokens, config.seed,
            TokenSink { onToken(it) },
        )

    override fun cancel() = NativeBridge.nativeCancel()

    override fun release() = NativeBridge.nativeRelease()
}
