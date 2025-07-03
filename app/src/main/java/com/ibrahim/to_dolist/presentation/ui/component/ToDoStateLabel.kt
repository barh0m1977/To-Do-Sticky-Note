package com.ibrahim.to_dolist.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ibrahim.to_dolist.data.model.ToDoState

@Composable
fun ToDoStateLabel(
    state: ToDoState,
    isSelected: Boolean = false,
    onClick: (ToDoState) -> Unit
) {
//     val backgroundColor = when (state) {
//        ToDoState.PENDING -> Color(0xFFFFF59D)
//        ToDoState.IN_PROGRESS -> Color(0xFF90CAF9)
//        ToDoState.DONE -> Color(0xFFA5D6A7)
//    }

    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val boxSize=(state.name.length+4).dp
    Box(
        modifier = Modifier
            .clickable { onClick(state) }
            .border(width = 2.dp, color = borderColor, shape = RoundedCornerShape(15.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = state.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
            color = Color.Black,
            fontSize = 14.sp
        )
    }
}
