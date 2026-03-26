package com.ibrahim.to_dolist.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices.PIXEL_9_PRO_XL
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ibrahim.to_dolist.data.model.ToDoState
import com.ibrahim.to_dolist.data.model.ToDoStickyColors
import com.ibrahim.to_dolist.presentation.ui.component.CardStickyNote
import com.ibrahim.to_dolist.presentation.util.CardStyle
import kotlinx.coroutines.delay

@Composable
fun CardStyleOnboardingPage() {

    val listState = rememberLazyListState()
    val styles = CardStyle.entries

    // Auto scrolling
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)

            val next = (listState.firstVisibleItemIndex + 1) % styles.size

            listState.animateScrollToItem(next)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Customize Your Cards",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Choose from multiple card styles to organize tasks your way.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(28.dp))

        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            itemsIndexed(styles) { _, style ->

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    CardStickyNote(
                        modifier = Modifier.width(220.dp),
                        text = "Example task",
                        colorArray = ToDoStickyColors.PEACH_BLOSSOM,
                        state = ToDoState.IN_PROGRESS,
                        cardStyle = style,
                        previewMode = true
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = style.name
                            .lowercase()
                            .replace("_", " ")
                            .replaceFirstChar { it.uppercase() },
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    device = PIXEL_9_PRO_XL,
    name = "Onboarding Features"
)
@Composable
fun OnboardingFeaturesPagePreview() {
    CardStyleOnboardingPage()
}