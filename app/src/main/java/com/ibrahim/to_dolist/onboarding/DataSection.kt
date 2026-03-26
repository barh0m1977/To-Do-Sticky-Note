package com.ibrahim.to_dolist.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ibrahim.to_dolist.presentation.ui.screens.settings.SettingsDataSectionPreview

@Composable
fun DataSection(){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Data Management",
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            text = "Import and export your tasks effortlessly, keeping your data under control.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        SettingsDataSectionPreview()
    }
}

