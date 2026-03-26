package com.ibrahim.to_dolist.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices.PIXEL_9_PRO_XL
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun GetStartedOnboardingPage(
    onGetStarted: () -> Unit,
) {
    // ── Entrance animations ───────────────────────────────────────────────────
    val iconAlpha  = remember { Animatable(0f) }
    val iconScale  = remember { Animatable(0.5f) }
    val textAlpha  = remember { Animatable(0f) }
    val textSlide  = remember { Animatable(24f) }
    val btnAlpha   = remember { Animatable(0f) }
    val btnSlide   = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        iconAlpha.animateTo(1f,  tween(500, easing = FastOutSlowInEasing))
        iconScale.animateTo(1f,  tween(500, easing = FastOutSlowInEasing))
        textAlpha.animateTo(1f,  tween(400, delayMillis = 200, easing = FastOutSlowInEasing))
        textSlide.animateTo(0f,  tween(400, delayMillis = 200, easing = FastOutSlowInEasing))
        btnAlpha.animateTo(1f,   tween(400, delayMillis = 400, easing = FastOutSlowInEasing))
        btnSlide.animateTo(0f,   tween(400, delayMillis = 400, easing = FastOutSlowInEasing))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        // ── Check icon ────────────────────────────────────────────────────────
        Icon(
            imageVector        = Icons.Default.CheckCircle,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.primary,
            modifier           = Modifier
                .size(96.dp)
                .alpha(iconAlpha.value)
                .graphicsLayer {
                    scaleX = iconScale.value
                    scaleY = iconScale.value
                },
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ── Headline ──────────────────────────────────────────────────────────
        Text(
            text       = "You're all set!",
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center,
            modifier   = Modifier
                .alpha(textAlpha.value)
                .graphicsLayer { translationY = textSlide.value },
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text      = "Start adding your tasks and take control of your day.",
            style     = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier  = Modifier
                .alpha(textAlpha.value)
                .graphicsLayer { translationY = textSlide.value },
        )

        Spacer(modifier = Modifier.height(48.dp))

        // ── CTA button ────────────────────────────────────────────────────────
        Button(
            onClick  = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .alpha(btnAlpha.value)
                .graphicsLayer { translationY = btnSlide.value },
            shape    = RoundedCornerShape(16.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Text(
                text       = "Get Started",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Preview(showBackground = true, device = PIXEL_9_PRO_XL)
@Composable
private fun GetStartedOnboardingPagePreview() {
    GetStartedOnboardingPage(onGetStarted = {})
}