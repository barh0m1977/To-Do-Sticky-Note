import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.ibrahim.to_dolist.data.model.ToDo
import com.ibrahim.to_dolist.data.model.ToDoState
import com.ibrahim.to_dolist.data.model.ToDoStickyColors
import com.ibrahim.to_dolist.presentation.ui.component.ColorCircle
import com.ibrahim.to_dolist.presentation.ui.component.ToDoStateLabel
import com.ibrahim.to_dolist.presentation.ui.screens.AnimatedPlaceholder
import com.ibrahim.to_dolist.presentation.ui.screens.isLeesThan
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun CardStickyNote(
    modifier: Modifier = Modifier,
    text: String,
    colorArray: ToDoStickyColors,
    state: ToDoState,
    onDeleteConfirmed: () -> Unit,
    onEditConfirmed: (ToDo) -> Unit
) {

    var showDialogDelete by remember { mutableStateOf(false) }
    var visibleDelete by remember { mutableStateOf(true) }
    var deleteTriggered by remember { mutableStateOf(false) }

    var showDialogEdit by remember { mutableStateOf(false) }
    var visibleEdit by remember { mutableStateOf(true) }
    var EditTriggered by remember { mutableStateOf(false) }
    var editTitle by remember { mutableStateOf(text) }
    var editColor by remember { mutableStateOf(colorArray) }
    var editState by remember { mutableStateOf(state) }

    AnimatedVisibility(
        visible = visibleDelete,
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

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.5f),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween

                    ) {

                        IconButton(onClick = { showDialogDelete = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        IconButton(onClick = {
                            showDialogEdit = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Text(
                        text = "______________________",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(0.5f)
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(0.5f)
                    )

                    Text(
                        text = state.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black,
                        modifier = Modifier.weight(1f),
                        fontSize = 12.sp
                    )


                }

            }
        }
    }

    if (showDialogDelete) {
        AlertDialog(
            onDismissRequest = { showDialogDelete = false },
            confirmButton = {
                TextButton(onClick = {
                    showDialogDelete = false
                    visibleDelete = false
                    deleteTriggered = true
                }) {
                    Text("confirm", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialogDelete = false }) {
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


    if (showDialogEdit) {
        AlertDialog(
            onDismissRequest = { showDialogEdit = false },
            title = { Text("Edit Task") },
            text = {
                Column {
                    Text("Edit task title:")
                    Spacer(Modifier.height(8.dp))
                    TextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        placeholder = {
                            AnimatedPlaceholder(textFieldValue = editTitle)
                        }
                    )

                    Spacer(Modifier.height(8.dp))
                    Text("Select color:")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ToDoStickyColors.entries.size) { index ->
                            val color = ToDoStickyColors.entries[index]
                            ColorCircle(
                                color = color.listColor[0],
                                isSelected = color == editColor
                            ) {
                                editColor = color
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text("Select State:")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ToDoState.entries.size) { index ->
                            val s = ToDoState.entries[index]
                            ToDoStateLabel(
                                state = s,
                                isSelected = s == editState,
                                onClick = { editState = it }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (isLeesThan(editTitle)) {
                        onEditConfirmed(
                            ToDo(
                                title = editTitle,
                                cardColor = editColor,
                                state = editState
                            )
                        )
                        showDialogEdit = false

                    }else{
                        showDialogEdit = true

                    }
                }) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialogEdit = false }) {
                    Text("Cancel")
                }
            }
        )
    }

}
