package com.ibrahim.to_dolist.presentation.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

class SettingSlider{

    @Composable
    fun LanguageSlider(
        currentLanguage: String,
        onLanguageSelected: (String) -> Unit
    ) {
        val languages = listOf("en", "ar")
        var sliderPosition by remember { mutableStateOf(languages.indexOf(currentLanguage).toFloat()) }

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "English")
                Text(text = "Arabic")
            }

            Slider(
                value = sliderPosition,
                onValueChange = { value ->
                    sliderPosition = value
                },
                onValueChangeFinished = {
                    val selectedLang = languages[sliderPosition.toInt()]
                    onLanguageSelected(selectedLang)
                },
                valueRange = 0f..(languages.size - 1).toFloat(),
                steps = languages.size - 2
            )
        }
    }
}
