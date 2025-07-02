import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.filled.Edit
import com.ibrahim.to_dolist.data.model.ToDoState
import com.ibrahim.to_dolist.data.model.ToDoStickyColors
import kotlinx.coroutines.delay

@Composable
fun CardStickyNote(
    modifier: Modifier = Modifier,
    text: String,
    colorArray: ToDoStickyColors,
    state: ToDoState,
    onDeleteConfirmed: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(true) }
    var deleteTriggered by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = visible,
        exit = fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f)
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                               colorArray.listColor[0],
                                colorArray.listColor[1]
                            )
                        )
                    )
            ) {


                // Folded corner
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cornerSize = 40.dp.toPx()
                    val path = Path().apply {
                        moveTo(0f, size.height)
                        lineTo(cornerSize, size.height)
                        lineTo(0f, size.height - cornerSize)
                        close()
                    }
                    drawPath(
                        path = path,
                        color = colorArray.listColor[2]
                    )
                }

                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        IconButton(onClick = { showDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        IconButton(onClick = {
                            // navigation to edit or popup edit
                        }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = state.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )



                }

            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    visible = false
                    deleteTriggered = true
                }) {
                    Text("confirm", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("cancel")
                }
            },
            title = {
                Text("Are you sure?")
            },
            text = {
                Text("this action cannot be undone")
            }
        )
    }

    if (deleteTriggered) {
        LaunchedEffect(Unit) {
            delay(300)  // مدة الانيميشن
            onDeleteConfirmed()
        }
    }
}
