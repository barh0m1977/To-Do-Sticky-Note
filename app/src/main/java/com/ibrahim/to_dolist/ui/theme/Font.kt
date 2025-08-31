package com.ibrahim.to_dolist.ui.theme


import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ibrahim.to_dolist.R

val MindListFont = FontFamily(
    Font(R.font.clash, FontWeight.Normal),

)
object MindTextStyles {
    val Title = TextStyle(
        fontFamily = MindListFont,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        color = Color.Black
    )

    val Body = TextStyle(
        fontFamily = MindListFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = Color.DarkGray
    )
}