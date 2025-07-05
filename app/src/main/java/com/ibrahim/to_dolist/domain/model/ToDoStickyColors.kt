package com.ibrahim.to_dolist.domain.model

import androidx.compose.ui.graphics.Color

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