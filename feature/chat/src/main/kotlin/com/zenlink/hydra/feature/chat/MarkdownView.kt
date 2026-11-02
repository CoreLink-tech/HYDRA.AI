package com.zenlink.hydra.feature.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenlink.hydra.core.designsystem.HydraColors
import com.zenlink.hydra.core.designsystem.HydraIcons
import com.zenlink.hydra.core.designsystem.HydraIconButton
import com.zenlink.hydra.core.designsystem.HydraMono
import com.zenlink.hydra.core.designsystem.HydraShapes
import com.zenlink.hydra.core.designsystem.hydra
import kotlinx.coroutines.delay

/** Renders an assistant reply as a document: headings, lists, quotes and highlighted code. */
@Composable
fun MarkdownText(source: String, modifier: Modifier = Modifier) {
    val blocks = remember(source) { MarkdownParser.parse(source) }
    val c = MaterialTheme.hydra
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        blocks.forEach { block ->
            when (block) {
                is MdBlock.Heading -> Text(
                    inlineText(block.text, c),
                    style = when (block.level) {
                        1 -> MaterialTheme.typography.headlineSmall
                        2 -> MaterialTheme.typography.titleLarge
                        else -> MaterialTheme.typography.titleMedium
                    },
                    color = c.textPrimary,
                    modifier = Modifier.padding(top = 4.dp),
                )
                is MdBlock.Paragraph -> Text(
                    inlineText(block.text, c),
                    style = MaterialTheme.typography.bodyLarge,
                    color = c.textPrimary,
                )
                is MdBlock.Bullets -> ListBlock(block.items.map { "•" to it }, c)
                is MdBlock.Numbered -> ListBlock(block.items.mapIndexed { i, t -> "${block.start + i}." to t }, c)
                is MdBlock.Quote -> Row(Modifier.height(IntrinsicSize.Min)) {
                    Box(Modifier.width(3.dp).fillMaxHeight().background(c.borderStrong))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        inlineText(block.text, c),
                        style = MaterialTheme.typography.bodyLarge,
                        color = c.textSecondary,
                        fontStyle = FontStyle.Italic,
                    )
                }
                is MdBlock.Code -> CodeBlock(block, c)
                MdBlock.Rule -> Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
            }
        }
    }
}

@Composable
private fun ListBlock(items: List<Pair<String, String>>, c: HydraColors) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { (marker, text) ->
            Row {
                Text(
                    marker,
                    style = MaterialTheme.typography.bodyLarge,
                    color = c.textMuted,
                    modifier = Modifier.widthIn(min = 22.dp),
                )
                Text(inlineText(text, c), style = MaterialTheme.typography.bodyLarge, color = c.textPrimary)
            }
        }
    }
}

@Composable
private fun CodeBlock(block: MdBlock.Code, c: HydraColors) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1500)
            copied = false
        }
    }
    val tokens = remember(block.code, block.language) { CodeHighlighter.tokenize(block.code, block.language) }
    val styled = remember(tokens, c) {
        buildAnnotatedString {
            tokens.forEach { t ->
                val color = when (t.kind) {
                    TokenKind.Keyword -> c.accent
                    TokenKind.Str, TokenKind.Number -> c.warning
                    TokenKind.Comment -> c.textMuted
                    TokenKind.Plain -> c.textPrimary
                }
                withStyle(
                    SpanStyle(
                        color = color,
                        fontStyle = if (t.kind == TokenKind.Comment) FontStyle.Italic else null,
                    ),
                ) { append(t.text) }
            }
        }
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(HydraShapes.button)
            .background(c.background)
            .border(1.dp, c.border, HydraShapes.button),
    ) {
        Row(
            Modifier.fillMaxWidth().background(c.surfaceElevated).padding(start = 14.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                block.language ?: "code",
                style = MaterialTheme.typography.labelMedium,
                color = c.textSecondary,
                modifier = Modifier.weight(1f),
            )
            if (copied) {
                Text("Copied", style = MaterialTheme.typography.labelMedium, color = c.accent, modifier = Modifier.padding(end = 10.dp))
            } else {
                HydraIconButton(
                    icon = HydraIcons.Copy,
                    contentDescription = "Copy code",
                    onClick = {
                        copyToClipboard(context, "Code", block.code)
                        copied = true
                    },
                )
            }
        }
        Box(Modifier.horizontalScroll(rememberScrollState()).padding(14.dp)) {
            Text(
                styled,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                softWrap = false,
            )
        }
    }
}

private fun inlineText(source: String, c: HydraColors): AnnotatedString = buildAnnotatedString {
    MarkdownParser.inline(source).forEach { run ->
        val style = SpanStyle(
            fontWeight = if (run.bold) FontWeight.SemiBold else null,
            fontStyle = if (run.italic) FontStyle.Italic else null,
            fontFamily = if (run.code) HydraMono else null,
            background = if (run.code) c.surfaceElevated else androidx.compose.ui.graphics.Color.Unspecified,
            color = if (run.link) c.accent else androidx.compose.ui.graphics.Color.Unspecified,
            textDecoration = if (run.link) TextDecoration.Underline else null,
        )
        withStyle(style) { append(run.text) }
    }
}

internal fun copyToClipboard(context: Context, label: String, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText(label, text))
}
