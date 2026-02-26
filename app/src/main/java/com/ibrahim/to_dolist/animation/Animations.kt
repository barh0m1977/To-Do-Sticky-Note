package com.ibrahim.to_dolist.animation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.ibrahim.to_dolist.R
import kotlinx.coroutines.delay

@Composable
fun AnimatedPlaceholder(textFieldValue: String) {
    val fullText = stringResource(R.string.task_text_here)
    var visibleText by remember { mutableStateOf("") }

    LaunchedEffect(key1 = textFieldValue.isEmpty()) {
        while (textFieldValue.isEmpty()) {
            for (i in 1..fullText.length) {
                visibleText = fullText.take(i)
                delay(100)
            }
            delay(500)
            visibleText = ""
            delay(300)
        }
    }

    if (textFieldValue.isEmpty()) {
        Text(visibleText, color = Color.Gray)
    }
}
