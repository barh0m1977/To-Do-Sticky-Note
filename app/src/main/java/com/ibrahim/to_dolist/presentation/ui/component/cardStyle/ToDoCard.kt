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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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

// Top-level constants — allocated once, never re-created on recompose
private val CardShape = RoundedCornerShape(24.dp)
private val CheckScaleSpec = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
)
private val ColorAnimSpec = tween<Color>(durationMillis = 300)

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
            MaterialTheme.colorScheme.onPrimaryContainer
        else
            MaterialTheme.colorScheme.onSurface,
        animationSpec = ColorAnimSpec,
        label = "titleColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.12f)
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AccentBar(accentColor)

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            CheckButton(
                isCompleted = isCompleted,
                accentColor = accentColor,
                onClick = { onCheckedChange(!isCompleted) },
            )
        }
    }
}

@Composable
private fun AccentBar(accentColor: Color) {
    Box(
        modifier = Modifier
            .width(4.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(50))
            .background(accentColor.copy(alpha = 0.8f))
    )
}

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

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (isCompleted) accentColor else Color.Transparent)
            .border(width = 2.dp, color = accentColor, shape = CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = if (isCompleted) "Mark incomplete" else "Mark complete",
            tint = if (isCompleted) MaterialTheme.colorScheme.onPrimary else Color.Transparent,
            modifier = Modifier
                .size(18.dp)
                .scale(checkScale),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ToDoCardPreview() {
    var isCompleted by remember { mutableStateOf(false) }
    ToDoCard(
        title = "Design new onboarding",
        subtitle = "Due today · High priority",
        isCompleted = isCompleted,
        onCheckedChange = { isCompleted = it },
    )
}