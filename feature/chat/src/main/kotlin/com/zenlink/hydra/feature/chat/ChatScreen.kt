package com.zenlink.hydra.feature.chat

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zenlink.hydra.core.designsystem.HydraBanner
import com.zenlink.hydra.core.designsystem.HydraCenteredContent
import com.zenlink.hydra.core.designsystem.HydraDialog
import com.zenlink.hydra.core.designsystem.HydraDivider
import com.zenlink.hydra.core.designsystem.HydraGroup
import com.zenlink.hydra.core.designsystem.HydraIconButton
import com.zenlink.hydra.core.designsystem.HydraIcons
import com.zenlink.hydra.core.designsystem.HydraMark
import com.zenlink.hydra.core.designsystem.HydraMotion
import com.zenlink.hydra.core.designsystem.HydraShapes
import com.zenlink.hydra.core.designsystem.HydraStatusDot
import com.zenlink.hydra.core.designsystem.HydraTag
import com.zenlink.hydra.core.designsystem.HydraTextField
import com.zenlink.hydra.core.designsystem.HydraTone
import com.zenlink.hydra.core.designsystem.LocalReducedMotion
import com.zenlink.hydra.core.designsystem.hydra
import com.zenlink.hydra.core.model.ChatMessage
import com.zenlink.hydra.core.model.ChatSummary
import com.zenlink.hydra.core.model.MessageRole
import com.zenlink.hydra.core.model.MessageStatus
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

private val suggestions = listOf(
    "Explain how photosynthesis works in simple terms",
    "Help me write a polite message to my landlord",
    "Make a study plan for my exam next week",
    "Give me three ideas for a weekend project",
)

/**
 * The chat. Everything comes from [ChatViewModel]; the only local state is the draft, whether the
 * history sheet is open, and whether the list should keep following new text.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    online: Boolean,
    onOpenModels: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var draft by rememberSaveable { mutableStateOf("") }
    var showHistory by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val openedAt = remember { System.currentTimeMillis() }

    // The list follows new text only while the reader is at the bottom. Scrolling up pauses it.
    var follow by remember { mutableStateOf(true) }
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .filter { !it }
            .collect { follow = !listState.canScrollForward }
    }
    LaunchedEffect(state.chatId) { follow = true }
    val last = state.messages.lastOrNull()
    LaunchedEffect(state.messages.size, last?.content?.length) {
        // Index == message count is the spacer after the last message, so the true end is shown.
        if (state.messages.isNotEmpty() && follow) listState.scrollToItem(state.messages.size)
    }

    val canSend = draft.isNotBlank() && !state.generating

    HydraCenteredContent(maxWidth = 780.dp) {
        ChatTopBar(
            modelName = state.modelName,
            modelReady = state.modelReady,
            online = online,
            canChangeChat = !state.generating,
            onHistory = { showHistory = true },
            onNewChat = {
                viewModel.newChat()
                draft = ""
            },
        )
        HydraDivider()

        state.notice?.let { notice ->
            HydraBanner(
                text = notice,
                tone = HydraTone.Warning,
                icon = HydraIcons.Info,
                actionLabel = "Models",
                onAction = onOpenModels,
                onDismiss = viewModel::dismissNotice,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }

        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (state.messages.isEmpty()) {
                EmptyChat(
                    modelName = state.modelName,
                    modelReady = state.modelReady,
                    onSuggestion = { draft = it },
                    onOpenModels = onOpenModels,
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    items(state.messages, key = { it.id }) { message ->
                        Entrance(enabled = message.createdAtMs >= openedAt) {
                            if (message.role == MessageRole.USER) {
                                UserMessage(message)
                            } else {
                                AssistantMessage(message)
                            }
                        }
                    }
                    item(key = "end") { Spacer(Modifier.height(4.dp)) }
                }
            }

            JumpToLatest(
                visible = !follow && state.messages.isNotEmpty(),
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                onClick = {
                    follow = true
                    scope.launch { listState.scrollToItem(state.messages.size) }
                },
            )
        }

        Composer(
            draft = draft,
            onDraft = { draft = it },
            generating = state.generating,
            awaitingFirstToken = state.generating && last?.role == MessageRole.ASSISTANT && last.content.isEmpty(),
            canSend = canSend,
            onSend = {
                follow = true
                viewModel.send(draft)
                draft = ""
            },
            onStop = viewModel::stop,
        )
    }

    if (showHistory) {
        HistorySheet(
            history = state.history,
            query = state.historyQuery,
            currentChatId = state.chatId,
            onQuery = viewModel::setHistoryQuery,
            onOpen = {
                viewModel.openChat(it)
                showHistory = false
            },
            onDelete = viewModel::deleteChat,
            onDismiss = {
                viewModel.setHistoryQuery("")
                showHistory = false
            },
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Top bar
// ---------------------------------------------------------------------------------------------

@Composable
private fun ChatTopBar(
    modelName: String?,
    modelReady: Boolean,
    online: Boolean,
    canChangeChat: Boolean,
    onHistory: () -> Unit,
    onNewChat: () -> Unit,
) {
    val c = MaterialTheme.hydra
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().height(64.dp).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(8.dp))
        HydraMark(markSize = 30.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("HYDRA.AI", style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                HydraStatusDot(if (modelName != null && modelReady) c.success else c.warning, size = 7.dp)
                Text(
                    modelName ?: "No model selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (!online) HydraTag("Offline", tone = HydraTone.Neutral)
            }
        }
        HydraIconButton(HydraIcons.History, "Conversation history", onHistory)
        HydraIconButton(HydraIcons.Add, "New conversation", onNewChat, enabled = canChangeChat)
    }
}

// ---------------------------------------------------------------------------------------------
// Messages
// ---------------------------------------------------------------------------------------------

/** Fades and lifts a message in once. Messages that were already on screen when it opened do not animate. */
@Composable
private fun Entrance(enabled: Boolean, content: @Composable () -> Unit) {
    if (!enabled || LocalReducedMotion.current) {
        content()
        return
    }
    val visible = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = visible,
        enter = fadeIn(tween(HydraMotion.Medium)) + slideInVertically(tween(HydraMotion.Medium)) { it / 10 },
    ) { content() }
}

@Composable
private fun UserMessage(message: ChatMessage) {
    val c = MaterialTheme.hydra
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Box(
            Modifier
                .widthIn(max = 560.dp)
                .clip(HydraShapes.bubble)
                .background(c.accentSoft)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            SelectionContainer {
                Text(message.content, style = MaterialTheme.typography.bodyLarge, color = c.textPrimary)
            }
        }
    }
}

@Composable
private fun AssistantMessage(message: ChatMessage) {
    val c = MaterialTheme.hydra
    val context = LocalContext.current
    val streaming = message.status == MessageStatus.STREAMING
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HydraMark(markSize = 18.dp)
            Spacer(Modifier.width(8.dp))
            Text("HYDRA", style = MaterialTheme.typography.labelMedium, color = c.textMuted)
        }
        Spacer(Modifier.height(8.dp))
        if (message.content.isEmpty() && streaming) {
            ThinkingDots()
            // Long messages take a while to read on a phone. Say so instead of looking frozen.
            var slow by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(6_000)
                slow = true
            }
            if (slow) {
                Text(
                    "Still reading your message. Longer messages take longer on a phone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.hydra.textMuted,
                )
            }
        } else {
            SelectionContainer { MarkdownText(message.content) }
        }
        if (message.status == MessageStatus.STOPPED) {
            Spacer(Modifier.height(8.dp))
            HydraTag("Stopped", tone = HydraTone.Neutral)
        }
        if (message.status == MessageStatus.FAILED) {
            Spacer(Modifier.height(8.dp))
            HydraTag("Reply failed", tone = HydraTone.Danger)
        }
        if (!streaming && message.content.isNotBlank()) {
            Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                HydraIconButton(
                    icon = HydraIcons.Copy,
                    contentDescription = "Copy reply",
                    onClick = { copyToClipboard(context, "HYDRA reply", message.content) },
                )
                Text(
                    DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(message.createdAtMs)),
                    style = MaterialTheme.typography.labelMedium,
                    color = c.textMuted,
                )
            }
        }
    }
}

/** Three softly pulsing dots shown before the first word of a reply arrives. */
@Composable
private fun ThinkingDots() {
    val c = MaterialTheme.hydra
    val reduced = LocalReducedMotion.current
    val transition = rememberInfiniteTransition(label = "thinking")
    Row(
        Modifier.height(28.dp).padding(start = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { index ->
            val alpha by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 150),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "dot$index",
            )
            Box(
                Modifier
                    .size(8.dp)
                    .alpha(if (reduced) 0.7f else alpha)
                    .clip(CircleShape)
                    .background(c.accent),
            )
        }
    }
}

/** Declared as its own function so the animation resolves to the plain AnimatedVisibility, not a Column-scoped one. */
@Composable
private fun JumpToLatest(visible: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val c = MaterialTheme.hydra
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(HydraMotion.Short)) + scaleIn(tween(HydraMotion.Short), initialScale = 0.8f),
        exit = fadeOut(tween(HydraMotion.Short)) + scaleOut(tween(HydraMotion.Short), targetScale = 0.8f),
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(c.surfaceElevated)
                .border(1.dp, c.borderStrong, RoundedCornerShape(12.dp))
                .clickable(role = Role.Button, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) { Icon(HydraIcons.ArrowDown, contentDescription = "Jump to latest message", tint = c.textPrimary, modifier = Modifier.size(22.dp)) }
    }
}

// ---------------------------------------------------------------------------------------------
// Empty state
// ---------------------------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmptyChat(
    modelName: String?,
    modelReady: Boolean,
    onSuggestion: (String) -> Unit,
    onOpenModels: () -> Unit,
) {
    val c = MaterialTheme.hydra
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        HydraMark(markSize = 56.dp)
        Spacer(Modifier.height(20.dp))
        Text("How can I help?", style = MaterialTheme.typography.headlineMedium, color = c.textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            "Replies are generated on this phone and never leave it.",
            style = MaterialTheme.typography.bodyMedium,
            color = c.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when {
                modelName == null -> HydraTag("No model selected", tone = HydraTone.Warning, modifier = Modifier.clickable(onClick = onOpenModels))
                modelReady -> {
                    HydraTag(modelName)
                    HydraTag("Runs on this phone", tone = HydraTone.Neutral)
                }
                else -> {
                    HydraTag(modelName, tone = HydraTone.Neutral)
                    HydraTag("Not downloaded yet", tone = HydraTone.Warning, modifier = Modifier.clickable(onClick = onOpenModels))
                }
            }
        }
        Spacer(Modifier.height(28.dp))
        HydraGroup(Modifier.widthIn(max = 560.dp)) {
            suggestions.forEachIndexed { index, text ->
                if (index > 0) HydraDivider()
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button) { onSuggestion(text) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text, style = MaterialTheme.typography.bodyMedium, color = c.textPrimary, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    Icon(HydraIcons.ChevronRight, contentDescription = null, tint = c.textMuted, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Composer
// ---------------------------------------------------------------------------------------------

@Composable
private fun Composer(
    draft: String,
    onDraft: (String) -> Unit,
    generating: Boolean,
    awaitingFirstToken: Boolean,
    canSend: Boolean,
    onSend: () -> Unit,
    onStop: () -> Unit,
) {
    val c = MaterialTheme.hydra
    var focused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        if (focused) c.accent.copy(alpha = 0.7f) else c.borderStrong,
        tween(HydraMotion.Short),
        label = "composerBorder",
    )
    val actionBg by animateColorAsState(
        when {
            generating -> c.card
            canSend -> c.accent
            else -> c.surface
        },
        tween(HydraMotion.Short),
        label = "actionBg",
    )
    val actionFg = when {
        generating -> c.textPrimary
        canSend -> c.onAccent
        else -> c.textMuted
    }

    Column(
        Modifier
            .fillMaxWidth()
            .background(c.background)
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 12.dp)
            .padding(top = 8.dp, bottom = 10.dp),
    ) {
        AnimatedVisibility(visible = generating) {
            Row(Modifier.padding(start = 6.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                HydraStatusDot(c.accent, size = 7.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (awaitingFirstToken) "Thinking on this phone…" else "Writing on this phone…",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.textSecondary,
                )
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .clip(HydraShapes.input)
                .background(c.surfaceElevated)
                .border(1.dp, borderColor, HydraShapes.input)
                .padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Box(Modifier.weight(1f).padding(vertical = 10.dp)) {
                BasicTextField(
                    value = draft,
                    onValueChange = onDraft,
                    modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.textPrimary),
                    cursorBrush = SolidColor(c.accent),
                    maxLines = 6,
                )
                if (draft.isEmpty()) {
                    Text("Message HYDRA", style = MaterialTheme.typography.bodyLarge, color = c.textMuted)
                }
            }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(actionBg)
                    .then(if (generating) Modifier.border(1.dp, c.borderStrong, RoundedCornerShape(12.dp)) else Modifier)
                    .clickable(
                        enabled = generating || canSend,
                        role = Role.Button,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = androidx.compose.material3.ripple(),
                        onClick = if (generating) onStop else onSend,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (generating) HydraIcons.Stop else HydraIcons.Send,
                    contentDescription = if (generating) "Stop generating" else "Send message",
                    tint = actionFg,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// History
// ---------------------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistorySheet(
    history: List<ChatSummary>,
    query: String,
    currentChatId: Long?,
    onQuery: (String) -> Unit,
    onOpen: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = MaterialTheme.hydra
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var pendingDelete by remember { mutableStateOf<ChatSummary?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = c.surfaceElevated,
        shape = HydraShapes.sheet,
    ) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(horizontal = 16.dp)) {
            Text("Conversations", style = MaterialTheme.typography.titleLarge, color = c.textPrimary)
            Text(
                "Stored only on this phone.",
                style = MaterialTheme.typography.bodySmall,
                color = c.textSecondary,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
            )
            HydraTextField(value = query, onValueChange = onQuery, placeholder = "Search conversations")
            Spacer(Modifier.height(12.dp))

            if (history.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                    Text(
                        if (query.isBlank()) "No conversations yet. Start one and it will appear here." else "Nothing matches “${query.trim()}”.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxWidth().weight(1f, fill = false).heightIn(max = 460.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items(history, key = { it.id }) { chat ->
                        HistoryRow(
                            chat = chat,
                            selected = chat.id == currentChatId,
                            onOpen = { onOpen(chat.id) },
                            onDelete = { pendingDelete = chat },
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    pendingDelete?.let { chat ->
        HydraDialog(
            title = "Delete this conversation?",
            onDismiss = { pendingDelete = null },
            confirmLabel = "Delete",
            destructive = true,
            onConfirm = {
                onDelete(chat.id)
                pendingDelete = null
            },
        ) {
            Text("“${chat.title}” and its ${chat.messageCount} messages will be removed from this phone. This cannot be undone.")
        }
    }
}

@Composable
private fun HistoryRow(chat: ChatSummary, selected: Boolean, onOpen: () -> Unit, onDelete: () -> Unit) {
    val c = MaterialTheme.hydra
    val whenText = DateUtils.getRelativeTimeSpanString(
        chat.updatedAtMs,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
    ).toString()
    Row(
        Modifier
            .fillMaxWidth()
            .clip(HydraShapes.button)
            .background(if (selected) c.accentSoft else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(role = Role.Button, onClick = onOpen)
            .padding(start = 14.dp, top = 4.dp, bottom = 4.dp, end = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
            Text(chat.title, style = MaterialTheme.typography.bodyLarge, color = c.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "${chat.messageCount} messages · $whenText",
                style = MaterialTheme.typography.bodySmall,
                color = c.textSecondary,
            )
        }
        HydraIconButton(HydraIcons.Delete, "Delete conversation", onDelete)
    }
}
