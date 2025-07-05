package com.ibrahim.to_dolist.data.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "todos")
data class ToDo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val cardColor: ToDoStickyColors = ToDoStickyColors.SUNRISE,
    val state: ToDoState = ToDoState.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis()
)

enum class ToDoState {
    PENDING,
    IN_PROGRESS,
    DONE
}

enum class ToDoStickyColors(val displayName: String, val listColor: List<Color>) {
    SUNRISE(
        "SUNRISE", listOf(
            Color(0xFFFDF5A6),
            Color(0xFFFCE97F),
            Color(0xFFEAD97C)
        )
    ),
    MINT_CREAM(
        "MINT CREAM", listOf(
            Color(0xFFE5FFCC),
            Color(0xFFD0F5A9),
            Color(0xFFB6E388)
        )
    ),

    PEACH_BLOSSOM(
        "PEACH BLOSSOM", listOf(
            Color(0xFFFFD1BA),
            Color(0xFFFFBFA3),
            Color(0xFFFFAA85)
        )
    ),

    SKY_MORNING(
        "SKY MORNING", listOf(
            Color(0xFFB9DFFF),
            Color(0xFFA0D2FF),
            Color(0xFF89C2FF)
        )
    ),


}
