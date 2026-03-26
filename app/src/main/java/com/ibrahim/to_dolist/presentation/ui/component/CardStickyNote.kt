package com.ibrahim.to_dolist.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ibrahim.to_dolist.R
import com.ibrahim.to_dolist.data.model.ToDoState
import com.ibrahim.to_dolist.data.model.ToDoStickyColors
import com.ibrahim.to_dolist.presentation.util.CardStyle

// ─── Public entry point ───────────────────────────────────────────────────────

@Composable
fun CardStickyNote(
    modifier          : Modifier          = Modifier,
    text              : String,
    colorArray        : ToDoStickyColors,
    state             : ToDoState,
    isLocked          : Boolean           = false,
    cardStyle         : CardStyle         = CardStyle.OUTLINED,
    previewMode       : Boolean           = false,
    onDeleteConfirmed : () -> Unit        = {},
    onEditConfirmed   : () -> Unit        = {},
    onClick           : () -> Unit        = {},
) {
    var isLockedState by rememberSaveable { mutableStateOf(isLocked) }
    LaunchedEffect(isLocked) { isLockedState = isLocked }

    val accent     = colorArray.listColor[1]
    val label      = if (isLockedState) "$text 🔒" else text
    val stateLabel = state.name
        .replace("_", " ")
        .lowercase()
        .replaceFirstChar { it.uppercase() }

    val cardParams = CardParams(
        modifier    = modifier,
        accent      = accent,
        colorArray  = colorArray,
        text        = label,
        state       = stateLabel,
        previewMode = previewMode,
        onDelete    = onDeleteConfirmed,
        onEdit      = onEditConfirmed,
        onClick     = onClick,
    )

    when (cardStyle) {
        CardStyle.MINIMAL    -> MinimalCard(cardParams)
        CardStyle.STATUS     -> StatusCard(cardParams)
        CardStyle.GLASS      -> GlassCard(cardParams)
        CardStyle.LAYERED    -> LayeredCard(cardParams)
        CardStyle.NEON_DARK  -> NeonDarkCard(cardParams)
        CardStyle.GRADIENT   -> GradientCard(cardParams)
        CardStyle.NEUMORPHIC -> NeumorphicCard(cardParams)
        CardStyle.OUTLINED   -> OutlinedCard(cardParams)
        CardStyle.ELEVATED   -> ElevatedCard(cardParams)
        CardStyle.RETRO      -> RetroCard(cardParams)
    }
}

// ─── Shared data class to avoid long parameter lists ─────────────────────────

@Stable
private data class CardParams(
    val modifier    : Modifier,
    val accent      : Color,
    val colorArray  : ToDoStickyColors,
    val text        : String,
    val state       : String,
    val previewMode : Boolean,
    val onDelete    : () -> Unit,
    val onEdit      : () -> Unit,
    val onClick     : () -> Unit,
)

// ─── Shared internals ─────────────────────────────────────────────────────────

@Composable
private fun CardActionRow(
    dotColor    : Color,
    iconTint    : Color,
    previewMode : Boolean,
    onDelete    : () -> Unit,
    onEdit      : () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        // Status dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor),
        )

        if (previewMode) {
            // Decorative placeholder — no interaction
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
            )
        } else {
            var menuExpanded by remember { mutableStateOf(false) }

            Box {
                IconButton(
                    onClick  = { menuExpanded = true },
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector        = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint               = iconTint,
                        modifier           = Modifier.size(16.dp),
                    )
                }

                DropdownMenu(
                    expanded          = menuExpanded,
                    onDismissRequest  = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                Icons.Default.Edit, null,
                                modifier = Modifier.size(16.dp),
                                tint     = MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        text    = { Text(stringResource(R.string.edit)) },
                        onClick = { menuExpanded = false; onEdit() },
                    )

                    HorizontalDivider()

                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete, null,
                                modifier = Modifier.size(16.dp),
                                tint     = MaterialTheme.colorScheme.error,
                            )
                        },
                        text    = { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) },
                        onClick = { menuExpanded = false; onDelete() },
                    )
                }
            }
        }
    }
}

@Composable
private fun SimpleBadge(label: String, textColor: Color, bgColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 9.dp, vertical = 3.dp),
    ) {
        Text(
            text          = label,
            fontSize      = 9.sp,
            fontWeight    = FontWeight.ExtraBold,
            color         = textColor,
            letterSpacing = 0.8.sp,
        )
    }
}

// ─── Modifier extensions ──────────────────────────────────────────────────────

private fun Modifier.noRippleClick(onClick: () -> Unit) = clickable(
    indication        = null,
    interactionSource = MutableInteractionSource(),
    onClick           = onClick,
)

private fun Modifier.hardShadow(
    color        : Color,
    cornerRadius : Dp,
    offset       : Dp = 4.dp,
) = drawBehind {
    drawRoundRect(
        color        = color,
        topLeft      = Offset(offset.toPx(), offset.toPx()),
        size         = Size(size.width, size.height),
        cornerRadius = CornerRadius(cornerRadius.toPx()),
    )
}

// ─── 1. Minimal ───────────────────────────────────────────────────────────────

@Composable
private fun MinimalCard(p: CardParams) {
    Box(
        modifier = p.modifier
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .noRippleClick(p.onClick)
            .padding(horizontal = 12.dp)
            .padding(top = 10.dp, bottom = 13.dp),
    ) {
        Column {
            CardActionRow(p.accent, MaterialTheme.colorScheme.onSurface.copy(0.3f), p.previewMode, p.onDelete, p.onEdit)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(0.5.dp).background(MaterialTheme.colorScheme.outlineVariant))
            Spacer(Modifier.height(9.dp))
            Text(p.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            Text(p.state, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(0.45f), letterSpacing = 1.sp)
        }
    }
}

// ─── 2. Status ────────────────────────────────────────────────────────────────

@Composable
private fun StatusCard(p: CardParams) {
    Column(
        modifier = p.modifier
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .noRippleClick(p.onClick),
    ) {
        Box(Modifier.fillMaxWidth().height(3.dp).background(p.accent))
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(top = 10.dp, bottom = 13.dp),
        ) {
            CardActionRow(p.accent, MaterialTheme.colorScheme.onSurface.copy(0.3f), p.previewMode, p.onDelete, p.onEdit)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(0.5.dp).background(p.accent.copy(0.15f)))
            Spacer(Modifier.height(9.dp))
            Text(p.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            SimpleBadge(p.state, p.accent, p.accent.copy(0.1f))
        }
    }
}

// ─── 3. Glass ─────────────────────────────────────────────────────────────────

@Composable
private fun GlassCard(p: CardParams) {
    Box(
        modifier = p.modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        p.colorArray.listColor[0].copy(alpha = 0.55f),
                        p.colorArray.listColor[1].copy(alpha = 0.35f),
                    )
                )
            )
            .border(1.dp, Color.White.copy(0.4f), RoundedCornerShape(16.dp))
            .noRippleClick(p.onClick)
            .padding(horizontal = 12.dp)
            .padding(top = 10.dp, bottom = 13.dp),
    ) {
        Column {
            CardActionRow(Color.White.copy(0.8f), Color.Black.copy(0.5f), p.previewMode, p.onDelete, p.onEdit)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(0.5.dp).background(Color.White.copy(0.3f)))
            Spacer(Modifier.height(9.dp))
            Text(p.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            Spacer(Modifier.height(8.dp))
            Text(p.state, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                color = Color.Black.copy(0.7f), letterSpacing = 1.sp)
        }
    }
}

// ─── 4. Layered ───────────────────────────────────────────────────────────────

@Composable
private fun LayeredCard(p: CardParams) {
    Box(
        modifier = p.modifier
            .drawBehind {
                val cr = CornerRadius(12.dp.toPx())
                drawRoundRect(p.accent.copy(0.35f), topLeft = Offset(6.dp.toPx(), 8.dp.toPx()),
                    size = Size(size.width, size.height), cornerRadius = cr)
                drawRoundRect(p.accent.copy(0.55f), topLeft = Offset(3.dp.toPx(), 4.dp.toPx()),
                    size = Size(size.width, size.height), cornerRadius = cr)
            }
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, p.accent.copy(0.3f), RoundedCornerShape(12.dp))
            .noRippleClick(p.onClick)
            .padding(horizontal = 12.dp)
            .padding(top = 10.dp, bottom = 13.dp),
    ) {
        Column {
            CardActionRow(p.accent, MaterialTheme.colorScheme.onSurface.copy(0.3f), p.previewMode, p.onDelete, p.onEdit)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(0.5.dp).background(p.accent.copy(0.2f)))
            Spacer(Modifier.height(9.dp))
            Text(p.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            Text(p.state, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                color = p.accent, letterSpacing = 1.sp)
        }
    }
}

// ─── 5. Neon Dark ─────────────────────────────────────────────────────────────

@Composable
private fun NeonDarkCard(p: CardParams) {
    Box(
        modifier = p.modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0F0F1A))
            .border(1.dp, p.accent.copy(0.4f), RoundedCornerShape(14.dp))
            .noRippleClick(p.onClick)
            .padding(horizontal = 12.dp)
            .padding(top = 10.dp, bottom = 13.dp),
    ) {
        Column {
            CardActionRow(p.accent, p.accent.copy(0.45f), p.previewMode, p.onDelete, p.onEdit)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(0.5.dp).background(p.accent.copy(0.2f)))
            Spacer(Modifier.height(9.dp))
            Text(p.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                color = Color(0xFFE0FFF8))
            Spacer(Modifier.height(8.dp))
            SimpleBadge(p.state, p.accent, p.accent.copy(0.12f))
        }
    }
}

// ─── 6. Gradient ──────────────────────────────────────────────────────────────

@Composable
private fun GradientCard(p: CardParams) {
    Box(
        modifier = p.modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    listOf(p.colorArray.listColor[0], p.colorArray.listColor[1]),
                    start = Offset.Zero,
                    end   = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                )
            )
            .noRippleClick(p.onClick)
            .padding(horizontal = 12.dp)
            .padding(top = 10.dp, bottom = 13.dp),
    ) {
        Column {
            CardActionRow(Color.White.copy(0.9f), Color.White.copy(0.55f), p.previewMode, p.onDelete, p.onEdit)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(0.5.dp).background(Color.White.copy(0.25f)))
            Spacer(Modifier.height(9.dp))
            Text(p.text, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text(p.state, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold,
                color = Color.White.copy(0.75f), letterSpacing = 1.sp)
        }
    }
}

// ─── 7. Neumorphic ────────────────────────────────────────────────────────────

private val NeumorphicBg    = Color(0xFFE0E5EC)
private val NeumorphicDark  = Color(0xFFB8BEC7)
private val NeumorphicLight = Color(0xFFFFFFFF)
private val NeumorphicText  = Color(0xFF31445E)
private val NeumorphicSub   = Color(0xFF7A8FA8)

@Composable
private fun NeumorphicCard(p: CardParams) {
    Box(
        modifier = p.modifier
            .clip(RoundedCornerShape(16.dp))
            .background(NeumorphicBg)
            .shadow(0.dp)
            .drawBehind {
                val cr = CornerRadius(16.dp.toPx())
                drawRoundRect(NeumorphicDark,
                    topLeft = Offset(6.dp.toPx(), 6.dp.toPx()),
                    size = size, cornerRadius = cr)
                drawRoundRect(NeumorphicLight,
                    topLeft = Offset(-4.dp.toPx(), -4.dp.toPx()),
                    size = size, cornerRadius = cr)
                drawRoundRect(NeumorphicBg, size = size, cornerRadius = cr)
            }
            .noRippleClick(p.onClick)
            .padding(horizontal = 12.dp)
            .padding(top = 10.dp, bottom = 13.dp),
    ) {
        Column {
            CardActionRow(p.accent, NeumorphicText.copy(0.35f), p.previewMode, p.onDelete, p.onEdit)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(0.5.dp).background(NeumorphicDark))
            Spacer(Modifier.height(9.dp))
            Text(p.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NeumorphicText)
            Spacer(Modifier.height(8.dp))
            Text(p.state, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                color = NeumorphicSub, letterSpacing = 1.sp)
        }
    }
}

// ─── 8. Outlined ─────────────────────────────────────────────────────────────

@Composable
private fun OutlinedCard(p: CardParams) {
    Box(
        modifier = p.modifier
            .hardShadow(p.accent, 10.dp)
            .border(2.dp, p.accent, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.background)
            .noRippleClick(p.onClick)
            .padding(horizontal = 12.dp)
            .padding(top = 10.dp, bottom = 13.dp),
    ) {
        Column {
            CardActionRow(p.accent, MaterialTheme.colorScheme.onBackground.copy(0.35f), p.previewMode, p.onDelete, p.onEdit)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(p.accent.copy(0.18f)))
            Spacer(Modifier.height(9.dp))
            Text(p.text, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, p.accent.copy(0.4f), RoundedCornerShape(20.dp))
                    .background(p.accent.copy(0.08f))
                    .padding(horizontal = 9.dp, vertical = 3.dp),
            ) {
                Text(p.state, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold,
                    color = p.accent, letterSpacing = 0.8.sp)
            }
        }
    }
}

// ─── 9. Elevated ─────────────────────────────────────────────────────────────

@Composable
private fun ElevatedCard(p: CardParams) {
    Box(
        modifier = p.modifier
            .shadow(8.dp, RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(0.1f),
                spotColor    = Color.Black.copy(0.5f))
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .noRippleClick(p.onClick)
            .padding(horizontal = 12.dp)
            .padding(top = 10.dp, bottom = 13.dp),
    ) {
        Column {
            CardActionRow(p.accent, MaterialTheme.colorScheme.onSurface.copy(0.3f), p.previewMode, p.onDelete, p.onEdit)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(0.5.dp).background(MaterialTheme.colorScheme.outlineVariant))
            Spacer(Modifier.height(9.dp))
            Text(p.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            SimpleBadge(p.state, p.accent, p.accent.copy(0.1f))
        }
    }
}

// ─── 10. Retro ───────────────────────────────────────────────────────────────

private val RetroBackground = Color(0xFFF5F0E8)
private val RetroText       = Color(0xFF2C1A0E)
private val RetroSubText    = Color(0xFF8B6A3E)

@Composable
private fun RetroCard(p: CardParams) {
    Box(
        modifier = p.modifier
            .drawBehind {
                val cr = CornerRadius(4.dp.toPx())
                drawRoundRect(p.accent.copy(0.5f),
                    topLeft = Offset(3.dp.toPx(), 3.dp.toPx()),
                    size = Size(size.width, size.height), cornerRadius = cr)
                drawRoundRect(p.accent.copy(0.25f),
                    topLeft = Offset(5.dp.toPx(), 5.dp.toPx()),
                    size = Size(size.width, size.height), cornerRadius = cr)
            }
            .border(2.dp, p.accent, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .background(RetroBackground)
            .noRippleClick(p.onClick)
            .padding(horizontal = 12.dp)
            .padding(top = 10.dp, bottom = 13.dp),
    ) {
        Column {
            CardActionRow(p.accent, p.accent.copy(0.4f), p.previewMode, p.onDelete, p.onEdit)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(2.dp).background(p.accent.copy(0.35f)))
            Spacer(Modifier.height(9.dp))
            Text(p.text, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif, color = RetroText)
            Spacer(Modifier.height(8.dp))
            Text(p.state.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Serif, color = RetroSubText, letterSpacing = 2.sp)
        }
    }
}
@Composable
fun CardStylePreview() {

    val colors = ToDoStickyColors.PEACH_BLOSSOM

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        CardStickyNote(
            text = "Plan weekend trip",
            colorArray = colors,
            state = ToDoState.PENDING,
            cardStyle = CardStyle.MINIMAL,
            previewMode = true
        )

        CardStickyNote(
            text = "Finish UI design",
            colorArray = colors,
            state = ToDoState.IN_PROGRESS,
            cardStyle = CardStyle.GRADIENT,
            previewMode = true
        )

        CardStickyNote(
            text = "Submit project",
            colorArray = colors,
            state = ToDoState.DONE,
            cardStyle = CardStyle.GLASS,
            previewMode = true
        )
    }
}