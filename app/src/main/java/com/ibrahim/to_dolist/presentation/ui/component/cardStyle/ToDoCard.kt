package com.ibrahim.to_dolist.presentation.ui.component.cardStyle

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// ─── Animation & Shape Constants ───────────────────────────────────────────────

private val CardShape = RoundedCornerShape(16.dp)
private val CheckScaleSpec = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
)
private val ColorAnimSpec = tween<Color>(durationMillis = 300)

// ─── Composable ────────────────────────────────────────────────────────────────

/**
 * A modern to-do card with:
 * • Better color contrast: uses alpha 0.08f for softer background, accent color for accent bar
 * • Smooth animations for completion state
 * • Adaptive colors based on completion status
 * • Elegant left accent bar (no washed-out look)
 */
@Composable
fun ToDoCard(
    title: String,
    subtitle: String,
    isCompleted: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
) {
    val titleColor by animateColorAsState(
        targetValue = if (isCompleted)
            MaterialTheme.colorScheme.outline
        else
            MaterialTheme.colorScheme.onSurface,
        animationSpec = ColorAnimSpec,
        label = "titleColor"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isCompleted)
            accentColor.copy(alpha = 0.08f)
        else
            accentColor.copy(alpha = 0.2f),
        animationSpec = ColorAnimSpec,
        label = "containerColor"
    )

    val accentBarColor by animateColorAsState(
        targetValue = if (isCompleted)
           accentColor.copy(alpha = 0.8f)
        else
            accentColor.copy(alpha = 0.8f),
        animationSpec = ColorAnimSpec,
        label = "accentBarColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Accent bar (vibrant & visible) ──────────────────────────────────
            AccentBar(accentBarColor)

            Spacer(modifier = Modifier.width(12.dp))

            // ── Task content ───────────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = titleColor,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                )
                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // ── Checkbox ───────────────────────────────────────────────────────
            CheckButton(
                isCompleted = isCompleted,
                accentColor = accentColor,
                onClick = { onCheckedChange(!isCompleted) },
            )
        }
    }
}

// ─── Accent Bar Component ──────────────────────────────────────────────────────

/**
 * Left accent bar that shows the task status.
 * Uses full accent color for active tasks, muted for completed.
 */
@Composable
private fun AccentBar(color: Color) {
    Box(
        modifier = Modifier
            .width(3.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(topEnd = 50F, bottomEnd = 50F))
            .background(color)
    )
}

// ─── Check Button Component ────────────────────────────────────────────────────

/**
 * Circular checkbox with smooth scale animation on check/uncheck.
 * Shows filled state with checkmark when completed.
 */
@Composable
private fun CheckButton(
    isCompleted: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
) {
    val checkScale by animateFloatAsState(
        targetValue = if (isCompleted) 1f else 0f,
        animationSpec = CheckScaleSpec,
        label = "checkScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isCompleted) accentColor else Color.Transparent,
        animationSpec = ColorAnimSpec,
        label = "backgroundColor"
    )

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = accentColor,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = if (isCompleted) "Mark incomplete" else "Mark complete",
            tint = if (isCompleted) Color.White else Color.Transparent,
            modifier = Modifier
                .size(16.dp)
                .scale(checkScale),
        )
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun ToDoCardPreview() {
    var isCompleted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            "Active Task",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ToDoCard(
            title = "Design new onboarding flow",
            subtitle = "Due today · High priority",
            isCompleted = false,
            onCheckedChange = { isCompleted = it },
            accentColor = Color(0xFF448AFF),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Completed Task",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ToDoCard(
            title = "Review code changes",
            subtitle = "Completed yesterday",
            isCompleted = true,
            onCheckedChange = { isCompleted = false },
            accentColor = Color(0xFF00C853),
        )
    }
}