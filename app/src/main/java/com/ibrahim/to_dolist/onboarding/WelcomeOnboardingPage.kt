package com.ibrahim.to_dolist.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices.PIXEL_9_PRO_XL
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ibrahim.to_dolist.R

@Composable
fun WelcomeOnboardingPage() {

    // ── Entrance animations ───────────────────────────────────────────────────
    val iconAlpha = remember { Animatable(0f) }
    val iconScale = remember { Animatable(0.6f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleSlide = remember { Animatable(30f) }
    val bodyAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Icon pops in
        iconAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
        iconScale.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
        // Title slides up
        titleAlpha.animateTo(1f, tween(400, delayMillis = 150, easing = FastOutSlowInEasing))
        titleSlide.animateTo(0f, tween(400, delayMillis = 150, easing = FastOutSlowInEasing))
        // Body fades in
        bodyAlpha.animateTo(1f, tween(400, delayMillis = 300, easing = FastOutSlowInEasing))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        // ── App icon / illustration ───────────────────────────────────────────
        Image(
            painter = painterResource(id = R.drawable.to_do),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .alpha(iconAlpha.value)
                .size(120.dp)
                .clip(RoundedCornerShape(80.dp))
                .graphicsLayer {
                    scaleX = iconScale.value
                    scaleY = iconScale.value
                }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ── Headline ──────────────────────────────────────────────────────────
        Text(
            text = "Welcome to MindList",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .alpha(titleAlpha.value)
                .graphicsLayer { translationY = titleSlide.value },
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Sub-copy ──────────────────────────────────────────────────────────
        Text(
            text = "Stay focused, organized, and in control with our smart and simple To-Do List app.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.alpha(bodyAlpha.value),
        )
    }
}

@Preview(showBackground = true, device = PIXEL_9_PRO_XL)
@Composable
private fun WelcomeOnboardingPagePreview() {
    WelcomeOnboardingPage()
}