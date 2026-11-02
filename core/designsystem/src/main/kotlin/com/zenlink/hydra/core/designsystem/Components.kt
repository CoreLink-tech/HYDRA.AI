package com.zenlink.hydra.core.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ---------------------------------------------------------------------------------------------
// Layout helpers
// ---------------------------------------------------------------------------------------------

/** Centres content and caps its width so phones, tablets and landscape all read comfortably. */
@Composable
fun HydraCenteredContent(
    modifier: Modifier = Modifier,
    maxWidth: Dp = 680.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = maxWidth).fillMaxSize(), content = content)
    }
}

@Composable
fun HydraTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigation: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .heightIn(min = 60.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (navigation != null) navigation() else Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.hydra.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        actions()
    }
}

@Composable
fun HydraIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = MaterialTheme.hydra.textSecondary,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = tint,
            disabledContentColor = MaterialTheme.hydra.textMuted,
        ),
    ) { Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(22.dp)) }
}

// ---------------------------------------------------------------------------------------------
// Buttons
// ---------------------------------------------------------------------------------------------

@Composable
private fun ButtonLabel(text: String, icon: ImageVector?, loading: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = LocalButtonContent.current)
        } else if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

private val LocalButtonContent = androidx.compose.runtime.compositionLocalOf { Color.Unspecified }

@Composable
fun HydraPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
) {
    val c = MaterialTheme.hydra
    val interaction = remember { MutableInteractionSource() }
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp).pressScale(interaction),
        interactionSource = interaction,
        shape = HydraShapes.button,
        colors = ButtonDefaults.buttonColors(
            containerColor = c.accent,
            contentColor = c.onAccent,
            disabledContainerColor = c.surfaceElevated,
            disabledContentColor = c.textMuted,
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    ) {
        androidx.compose.runtime.CompositionLocalProvider(LocalButtonContent provides c.onAccent) {
            ButtonLabel(text, icon, loading)
        }
    }
}

@Composable
fun HydraSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    val c = MaterialTheme.hydra
    val interaction = remember { MutableInteractionSource() }
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp).pressScale(interaction),
        interactionSource = interaction,
        shape = HydraShapes.button,
        border = BorderStroke(1.dp, if (enabled) c.borderStrong else c.border),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = c.textPrimary, disabledContentColor = c.textMuted),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
    ) {
        androidx.compose.runtime.CompositionLocalProvider(LocalButtonContent provides c.textPrimary) {
            ButtonLabel(text, icon, loading = false)
        }
    }
}

/** For destructive actions such as deleting a model or all data. */
@Composable
fun HydraDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    val c = MaterialTheme.hydra
    val interaction = remember { MutableInteractionSource() }
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp).pressScale(interaction),
        interactionSource = interaction,
        shape = HydraShapes.button,
        border = BorderStroke(1.dp, if (enabled) c.danger.copy(alpha = 0.6f) else c.border),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = c.danger, disabledContentColor = c.textMuted),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
    ) {
        androidx.compose.runtime.CompositionLocalProvider(LocalButtonContent provides c.danger) {
            ButtonLabel(text, icon, loading = false)
        }
    }
}

@Composable
fun HydraTextButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.hydra.accent,
            disabledContentColor = MaterialTheme.hydra.textMuted,
        ),
    ) { Text(text, style = MaterialTheme.typography.labelLarge) }
}

// ---------------------------------------------------------------------------------------------
// Surfaces
// ---------------------------------------------------------------------------------------------

/** A quiet bordered card. Pass [onClick] to make the whole card tappable. */
@Composable
fun HydraCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    highlighted: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = MaterialTheme.hydra
    val border = BorderStroke(1.dp, if (highlighted) c.accent.copy(alpha = 0.55f) else c.border)
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = HydraShapes.card,
            color = c.card,
            border = border,
        ) { Column(Modifier.padding(contentPadding), content = content) }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = HydraShapes.card,
            color = c.card,
            border = border,
        ) { Column(Modifier.padding(contentPadding), content = content) }
    }
}

@Composable
fun HydraSectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.hydra.textMuted,
        modifier = modifier.padding(horizontal = 4.dp, vertical = 8.dp),
    )
}

@Composable
fun HydraDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier, thickness = 1.dp, color = MaterialTheme.hydra.border)
}

@Composable
fun HydraKeyValue(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.hydra.textSecondary)
        Spacer(Modifier.width(12.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.hydra.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** A settings-style row: optional icon, title, subtitle and a trailing control or chevron. */
@Composable
fun HydraListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val c = MaterialTheme.hydra
    val base = if (onClick != null) modifier.clickable(role = Role.Button, onClick = onClick) else modifier
    Row(
        base.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = c.textSecondary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(14.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = c.textPrimary)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        } else if (onClick != null) {
            Icon(HydraIcons.ChevronRight, contentDescription = null, tint = c.textMuted, modifier = Modifier.size(20.dp))
        }
    }
}

/** Groups [HydraListRow]s inside one bordered card with dividers drawn by the caller. */
@Composable
fun HydraGroup(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val c = MaterialTheme.hydra
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = HydraShapes.card,
        color = c.card,
        border = BorderStroke(1.dp, c.border),
    ) { Column(content = content) }
}

// ---------------------------------------------------------------------------------------------
// Controls
// ---------------------------------------------------------------------------------------------

@Composable
fun HydraSwitchRow(
    title: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
) {
    val c = MaterialTheme.hydra
    Row(
        modifier
            .fillMaxWidth()
            .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onChange)
            .heightIn(min = 56.dp)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = if (enabled) c.textPrimary else c.textMuted)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = c.onAccent,
                checkedTrackColor = c.accent,
                uncheckedThumbColor = c.textSecondary,
                uncheckedTrackColor = c.surfaceElevated,
                uncheckedBorderColor = c.borderStrong,
                disabledCheckedTrackColor = c.accent.copy(alpha = 0.4f),
                disabledUncheckedTrackColor = c.surfaceElevated,
            ),
        )
    }
}

/** A small segmented control with an animated selection, used for choices like the theme. */
@Composable
fun <T> HydraSegmented(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = MaterialTheme.hydra
    val reduced = LocalReducedMotion.current
    Row(
        modifier
            .fillMaxWidth()
            .clip(HydraShapes.button)
            .background(c.surface)
            .border(1.dp, c.border, HydraShapes.button)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        options.forEach { (value, label) ->
            val isSelected = value == selected
            val bg by animateColorAsState(
                if (isSelected) c.accentSoft else Color.Transparent,
                hydraTween(reduced, HydraMotion.Short),
                label = "segBg",
            )
            val fg by animateColorAsState(
                if (isSelected) c.accent else c.textSecondary,
                hydraTween(reduced, HydraMotion.Short),
                label = "segFg",
            )
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp)
                    .clip(HydraShapes.small)
                    .background(bg)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(value) }),
                contentAlignment = Alignment.Center,
            ) { Text(label, style = MaterialTheme.typography.labelLarge, color = fg) }
        }
    }
}

@Composable
fun HydraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true,
) {
    val c = MaterialTheme.hydra
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = singleLine,
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        shape = HydraShapes.input,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = c.accent,
            unfocusedBorderColor = c.borderStrong,
            focusedTextColor = c.textPrimary,
            unfocusedTextColor = c.textPrimary,
            cursorColor = c.accent,
            focusedLabelColor = c.accent,
            unfocusedLabelColor = c.textSecondary,
            focusedPlaceholderColor = c.textMuted,
            unfocusedPlaceholderColor = c.textMuted,
        ),
    )
}

@Composable
fun HydraDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmLabel: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String? = "Cancel",
    destructive: Boolean = false,
    confirmEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = MaterialTheme.hydra
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = HydraShapes.card,
        containerColor = c.surfaceElevated,
        titleContentColor = c.textPrimary,
        textContentColor = c.textSecondary,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp), content = content) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = confirmEnabled,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (destructive) c.danger else c.accent,
                    disabledContentColor = c.textMuted,
                ),
            ) { Text(confirmLabel, style = MaterialTheme.typography.labelLarge) }
        },
        dismissButton = if (dismissLabel != null) {
            {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = c.textSecondary),
                ) { Text(dismissLabel, style = MaterialTheme.typography.labelLarge) }
            }
        } else {
            null
        },
    )
}

// ---------------------------------------------------------------------------------------------
// Status and feedback
// ---------------------------------------------------------------------------------------------

enum class HydraTone { Neutral, Accent, Warning, Danger }

@Composable
fun HydraTag(text: String, modifier: Modifier = Modifier, tone: HydraTone = HydraTone.Accent) {
    val c = MaterialTheme.hydra
    val (bg, fg) = when (tone) {
        HydraTone.Neutral -> c.surfaceElevated to c.textSecondary
        HydraTone.Accent -> c.accentSoft to c.accent
        HydraTone.Warning -> c.warningSoft to c.warning
        HydraTone.Danger -> c.dangerSoft to c.danger
    }
    Box(modifier.clip(HydraShapes.tag).background(bg).padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = fg, maxLines = 1)
    }
}

@Composable
fun HydraStatusDot(color: Color, modifier: Modifier = Modifier, size: Dp = 8.dp) {
    Box(modifier.size(size).clip(CircleShape).background(color))
}

/** An inline message with a tone, an icon and an optional action. */
@Composable
fun HydraBanner(
    text: String,
    modifier: Modifier = Modifier,
    tone: HydraTone = HydraTone.Neutral,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
) {
    val c = MaterialTheme.hydra
    val (bg, fg) = when (tone) {
        HydraTone.Neutral -> c.surfaceElevated to c.textSecondary
        HydraTone.Accent -> c.accentSoft to c.accent
        HydraTone.Warning -> c.warningSoft to c.warning
        HydraTone.Danger -> c.dangerSoft to c.danger
    }
    Row(
        modifier
            .fillMaxWidth()
            .clip(HydraShapes.button)
            .background(bg)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(text, style = MaterialTheme.typography.bodyMedium, color = c.textPrimary, modifier = Modifier.weight(1f))
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction, colors = ButtonDefaults.textButtonColors(contentColor = fg)) {
                Text(actionLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
        if (onDismiss != null) {
            HydraIconButton(HydraIcons.Close, "Dismiss", onDismiss, tint = fg)
        }
    }
}

/** A thin determinate bar. A null [fraction] shows an indeterminate spinner instead. */
@Composable
fun HydraProgress(fraction: Float?, modifier: Modifier = Modifier) {
    val c = MaterialTheme.hydra
    if (fraction == null) {
        CircularProgressIndicator(modifier = modifier.size(24.dp), strokeWidth = 2.dp, color = c.accent, trackColor = c.border)
        return
    }
    val reduced = LocalReducedMotion.current
    val animated by animateFloatAsState(
        fraction.coerceIn(0f, 1f),
        hydraTween(reduced, HydraMotion.Medium),
        label = "progress",
    )
    Box(modifier.fillMaxWidth().height(6.dp).clip(HydraShapes.tag).background(c.border)) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(animated).clip(HydraShapes.tag).background(c.accent))
    }
}

@Composable
fun HydraSpinner(modifier: Modifier = Modifier, size: Dp = 24.dp) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        strokeWidth = 2.dp,
        color = MaterialTheme.hydra.accent,
        trackColor = MaterialTheme.hydra.border,
    )
}

/** Circular determinate progress with content (usually a percentage) in the middle. */
@Composable
fun HydraProgressRing(
    fraction: Float,
    modifier: Modifier = Modifier,
    size: Dp = 168.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val c = MaterialTheme.hydra
    val reduced = LocalReducedMotion.current
    val animated by animateFloatAsState(
        fraction.coerceIn(0f, 1f),
        hydraTween(reduced, HydraMotion.Medium),
        label = "ring",
    )
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 10.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            drawArc(
                color = c.border,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke),
            )
            drawArc(
                color = c.accent,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        content()
    }
}

/** A pulsing placeholder block shown while content loads. */
@Composable
fun HydraSkeleton(modifier: Modifier = Modifier) {
    val c = MaterialTheme.hydra
    val reduced = LocalReducedMotion.current
    val transition = rememberInfiniteTransition(label = "skeleton")
    val pulse by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "skeletonAlpha",
    )
    Box(modifier.clip(HydraShapes.small).background(c.surfaceElevated.copy(alpha = if (reduced) 0.6f else pulse)))
}

// ---------------------------------------------------------------------------------------------
// Navigation
// ---------------------------------------------------------------------------------------------

data class HydraNavItem(val route: String, val label: String, val icon: ImageVector)

/** Bottom navigation with an animated selection indicator. Handles the system navigation bar inset. */
@Composable
fun HydraNavigationBar(
    items: List<HydraNavItem>,
    selectedRoute: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = MaterialTheme.hydra
    Column(modifier.background(c.surface)) {
        HydraDivider()
        Row(Modifier.fillMaxWidth().navigationBarsPadding().height(64.dp)) {
            items.forEach { item ->
                NavButton(item, item.route == selectedRoute) { onSelect(item.route) }
            }
        }
    }
}

@Composable
private fun RowScope.NavButton(item: HydraNavItem, selected: Boolean, onClick: () -> Unit) {
    val c = MaterialTheme.hydra
    val reduced = LocalReducedMotion.current
    val indicator by animateColorAsState(
        if (selected) c.accentSoft else Color.Transparent,
        hydraTween(reduced, HydraMotion.Medium),
        label = "navIndicator",
    )
    val tint by animateColorAsState(
        if (selected) c.accent else c.textMuted,
        hydraTween(reduced, HydraMotion.Medium),
        label = "navTint",
    )
    Box(
        Modifier
            .weight(1f)
            .fillMaxHeight()
            .selectable(
                selected = selected,
                role = Role.Tab,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Box(
                Modifier.size(width = 56.dp, height = 30.dp).clip(RoundedCornerShape(10.dp)).background(indicator),
                contentAlignment = Alignment.Center,
            ) { Icon(item.icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp)) }
            Text(item.label, style = MaterialTheme.typography.labelMedium, color = tint)
        }
    }
}
