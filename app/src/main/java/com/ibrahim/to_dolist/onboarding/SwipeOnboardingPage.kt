package com.ibrahim.to_dolist.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices.PIXEL_9_PRO_XL
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ibrahim.to_dolist.presentation.ui.screens.SwipePreviewCard

@Composable
fun SwipeOnboardingPage() {

    val accent = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Swipe Tasks",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary

        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Swipe right to delete or left to edit your task.",
            style = MaterialTheme.typography.bodyMedium,

        )

        Spacer(modifier = Modifier.height(40.dp))

        SwipePreviewCard(accentColor = accent)
    }
}

@Preview(showBackground = true, name = "onboarding", device = PIXEL_9_PRO_XL)
@Composable
fun SwipeOnboardingPagePreview() {
    SwipeOnboardingPage()
}