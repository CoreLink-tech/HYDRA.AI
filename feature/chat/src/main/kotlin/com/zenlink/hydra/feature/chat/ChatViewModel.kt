package com.zenlink.hydra.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenlink.hydra.core.database.repo.ChatRepository
import com.zenlink.hydra.core.database.repo.ModelRepository
import com.zenlink.hydra.core.database.repo.SettingsRepository
import com.zenlink.hydra.core.llama.LlamaEngine
import com.zenlink.hydra.core.llama.LoadConfig
import com.zenlink.hydra.core.llama.SampleConfig
import com.zenlink.hydra.core.model.ChatMessage
import com.zenlink.hydra.core.model.ChatSummary
import com.zenlink.hydra.core.model.MessageRole
import com.zenlink.hydra.core.model.MessageStatus
import com.zenlink.hydra.core.model.ModelCatalog
import com.zenlink.hydra.core.model.ModelState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ChatUiState(
    val chatId: Long? = null,
    val messages: List<ChatMessage> = emptyList(),
    val generating: Boolean = false,
    /** Display name of the model chosen in Settings or Models, even before it has been loaded. */
    val modelName: String? = null,
    /** True when the chosen model's file is installed and can be loaded. */
    val modelReady: Boolean = false,
    /** Shown above the input when a reply cannot be made (no model, load failure, generation error). */
    val notice: String? = null,
    /** Past conversations, newest first. Filtered by [historyQuery] when it is not blank. */
    val history: List<ChatSummary> = emptyList(),
    val historyQuery: String = "",
)

/**
 * Drives one chat at a time. History comes from core:database; the reply being streamed is shown
 * live from [ReplyState] and written back to the database on a throttle and when it finishes or is
 * stopped. The same view-model also lists past conversations and switches between them.
 */
class ChatViewModel(
    private val chats: ChatRepository,
    private val models: ModelRepository,
    private val settings: SettingsRepository,
    private val engine: LlamaEngine,
    initialChatId: Long?,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState(chatId = initialChatId))
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private var turn: Job? = null
    private var historyJob: Job? = null
    private var liveId: Long? = null
    private var liveReply: ReplyState? = null
    private var dbMessages: List<ChatMessage> = emptyList()
    private var lastPersistMs = 0L

    /** Model currently loaded into the engine by this view-model, so a model switch triggers a reload. */
    private var loadedModelId: String? = null

    private val historyQuery = MutableStateFlow("")

    init {
        initialChatId?.let { observeHistory(it) }

        // Keeps the model indicator current when the user switches or installs a model.
        viewModelScope.launch {
            combine(settings.observe(), models.observeAll()) { prefs, records ->
                val id = prefs.activeModelId
                val name = id?.let { ModelCatalog.byId(it)?.displayName }
                val ready = id != null && records.any { it.modelId == id && it.state == ModelState.INSTALLED }
                name to ready
            }.collect { (name, ready) ->
                _state.update { it.copy(modelName = name, modelReady = ready) }
            }
        }

        // Conversation list for the history sheet, with optional search.
        viewModelScope.launch {
            combine(chats.observeChats(), historyQuery) { all, q -> all to q }
                .collectLatest { (all, q) ->
                    val list = if (q.isBlank()) all else chats.search(q)
                    _state.update { it.copy(history = list, historyQuery = q) }
                }
        }
    }

    fun send(text: String) {
        val prompt = text.trim()
        if (prompt.isEmpty() || _state.value.generating) return
        turn = viewModelScope.launch { runTurn(prompt) }
    }

    /** Stops the reply in progress. The partial text is kept and marked STOPPED. */
    fun stop() {
        if (_state.value.generating) turn?.cancel()
    }

    fun dismissNotice() = _state.update { it.copy(notice = null) }

    fun setHistoryQuery(query: String) {
        historyQuery.value = query
    }

    /** Starts a fresh conversation. The old one stays in history. Ignored while a reply is streaming. */
    fun newChat() {
        if (_state.value.generating) return
        historyJob?.cancel()
        dbMessages = emptyList()
        _state.update { it.copy(chatId = null, messages = emptyList(), notice = null) }
    }

    /** Opens a past conversation so the user can read it and continue it. */
    fun openChat(chatId: Long) {
        if (_state.value.generating) return
        if (_state.value.chatId == chatId) return
        dbMessages = emptyList()
        _state.update { it.copy(chatId = chatId, messages = emptyList(), notice = null) }
        observeHistory(chatId)
    }

    /** Deletes a conversation from this phone. The open chat is closed if it was the one deleted. */
    fun deleteChat(chatId: Long) {
        if (_state.value.generating && _state.value.chatId == chatId) return
        viewModelScope.launch {
            chats.delete(chatId)
            if (_state.value.chatId == chatId) newChat()
        }
    }

    private fun observeHistory(chatId: Long) {
        historyJob?.cancel()
        historyJob = viewModelScope.launch {
            chats.observeMessages(chatId).collect { list ->
                dbMessages = list
                publish()
            }
        }
    }

    /** Merges the database history with the live reply so the streaming bubble updates in place. */
    private fun publish() {
        val live = liveReply
        val id = liveId
        val merged = if (live != null && id != null) {
            dbMessages.map { m -> if (m.id == id) m.copy(content = live.text, status = live.status) else m }
        } else {
            dbMessages
        }
        _state.update { it.copy(messages = merged) }
    }

    private suspend fun runTurn(prompt: String) {
        _state.update { it.copy(generating = true, notice = null) }
        var assistantId: Long? = null
        var chatIdForPersist: Long? = null
        var reply = ReplyState()
        try {
            if (!ensureModelLoaded()) return

            val chatId = _state.value.chatId ?: withContext(NonCancellable) {
                val activeId = settings.observe().first().activeModelId
                chats.createChat(activeId).also { id ->
                    _state.update { it.copy(chatId = id) }
                    observeHistory(id)
                }
            }
            chatIdForPersist = chatId
            withContext(NonCancellable) {
                chats.appendMessage(chatId, MessageRole.USER, prompt, MessageStatus.COMPLETE)
                chats.autoTitleIfNew(chatId, prompt)
            }
            val newId = chats.appendMessage(chatId, MessageRole.ASSISTANT, "", MessageStatus.STREAMING)
            assistantId = newId
            liveId = newId
            liveReply = ReplyState()

            val history = chats.messagesFor(chatId).filter { it.id != newId }
            val fullPrompt = engine.buildPrompt(history)
            val mode = settings.observe().first().performanceMode
            val sample = SampleConfig(maxNewTokens = mode.maxNewTokens)

            engine.generate(fullPrompt, sample).collect { event ->
                reply = ReplyAssembler.apply(reply, event)
                liveReply = reply
                publish()
                maybePersistThrottled(chatId, newId, reply)
            }
        } catch (e: CancellationException) {
            // User tapped stop. Keep what was streamed and mark it STOPPED.
            reply = reply.copy(status = MessageStatus.STOPPED, finished = true)
            throw e
        } finally {
            withContext(NonCancellable) {
                val id = assistantId
                val chatId = chatIdForPersist
                if (id != null && chatId != null) {
                    val finalReply = if (reply.finished) reply else reply.copy(status = MessageStatus.STOPPED)
                    persist(chatId, id, finalReply)
                    if (finalReply.error != null) _state.update { it.copy(notice = finalReply.error) }
                }
                liveId = null
                liveReply = null
                _state.update { it.copy(generating = false) }
                publish()
            }
        }
    }

    private suspend fun ensureModelLoaded(): Boolean {
        val s = settings.observe().first()
        val id = s.activeModelId
        // Already loaded with the model the user wants: nothing to do.
        if (engine.isLoaded && id != null && id == loadedModelId) return true

        val record = id?.let { models.find(it) }
        val descriptor = id?.let { ModelCatalog.byId(it) }
        val path = record?.localPath
        if (record == null || descriptor == null || record.state != ModelState.INSTALLED || path == null) {
            _state.update { it.copy(notice = "No model is ready yet. Pick one in Models.") }
            return false
        }
        // The user switched models since the last load: release the old one first.
        if (engine.isLoaded) {
            engine.unload()
            loadedModelId = null
        }
        val error = engine.load(
            path,
            LoadConfig(contextTokens = descriptor.contextTokens, threads = s.performanceMode.threads),
        )
        if (error != null) {
            _state.update { it.copy(notice = "Could not load the model: $error") }
            return false
        }
        loadedModelId = id
        _state.update { it.copy(modelName = descriptor.displayName) }
        return true
    }

    private suspend fun maybePersistThrottled(chatId: Long, assistantId: Long, reply: ReplyState) {
        val now = System.currentTimeMillis()
        if (now - lastPersistMs < PERSIST_THROTTLE_MS) return
        lastPersistMs = now
        persist(chatId, assistantId, reply)
    }

    /** Writes text back, keeping the original createdAtMs so history order never changes. */
    private suspend fun persist(chatId: Long, assistantId: Long, reply: ReplyState) {
        val createdAt = dbMessages.firstOrNull { it.id == assistantId }?.createdAtMs
            ?: chats.messagesFor(chatId).firstOrNull { it.id == assistantId }?.createdAtMs
            ?: return
        chats.updateMessage(
            ChatMessage(assistantId, chatId, MessageRole.ASSISTANT, reply.text, reply.status, createdAt),
        )
    }

    companion object {
        const val PERSIST_THROTTLE_MS = 500L
    }
}
