import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.ibrahim.to_dolist.R
import com.ibrahim.to_dolist.data.model.ToDo
import com.ibrahim.to_dolist.data.model.ToDoState
import com.ibrahim.to_dolist.data.model.ToDoStickyColors
import com.ibrahim.to_dolist.presentation.ui.component.ColorCircle
import com.ibrahim.to_dolist.presentation.ui.component.ToDoStateLabel
import com.ibrahim.to_dolist.presentation.ui.screens.AnimatedPlaceholder
import com.ibrahim.to_dolist.presentation.ui.screens.isLeesThan
import com.ibrahim.to_dolist.util.BiometricHelper
import kotlinx.coroutines.delay

@Composable
fun CardStickyNote(
    modifier: Modifier = Modifier,
    text: String,
    colorArray: ToDoStickyColors,
    state: ToDoState,
    isLocked: Boolean,
    onDeleteConfirmed: () -> Unit,
    onEditConfirmed: (ToDo) -> Unit,
    onClick: () -> Unit
) {

    var showDialogDelete by rememberSaveable { mutableStateOf(false) }
    var deleteTriggered by rememberSaveable { mutableStateOf(false) }
    var showDialogEdit by rememberSaveable { mutableStateOf(false) }
    var editTitle by rememberSaveable { mutableStateOf(text) }
    var editColor by rememberSaveable { mutableStateOf(colorArray) }
    var editState by rememberSaveable { mutableStateOf(state) }
    var isLockedState by rememberSaveable { mutableStateOf(isLocked) }

    val context = LocalContext.current
    val activity = context as FragmentActivity
    LaunchedEffect(isLocked) {
        isLockedState = isLocked
    }
    fun authenticateThenShowDialog(showDialog: () -> Unit) {
        BiometricHelper(
            activity = activity,
            onSuccess = {
                showDialog()
            },
            onError = { msg ->
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        ).authenticate()
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
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
                    IconButton(onClick = {
                        if (isLocked) {
                            authenticateThenShowDialog { showDialogDelete = true }
                        } else {
                            showDialogDelete = true
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    IconButton(onClick = {
                        if (isLocked) {
                            authenticateThenShowDialog { showDialogEdit = true }
                        } else {
                            showDialogEdit = true
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                Text(
                    text = "______________________",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    modifier = Modifier.weight(0.5f)
                )
                Text(
                    text = text+if (isLocked) "🔒" else "",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    modifier = Modifier.weight(0.5f)
                )

                Text(
                    text = state.name.replace("_", " ").lowercase()
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    modifier = Modifier.weight(1f),
                    fontSize = 12.sp
                )
            }
        }
    }

    if (showDialogDelete) {
        AlertDialog(
            onDismissRequest = { showDialogDelete = false },
            confirmButton = {
                TextButton(onClick = {
                    showDialogDelete = false
                    deleteTriggered = true
                }) {
                    Text(stringResource(R.string.confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialogDelete = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = { Text(stringResource(R.string.are_you_sure)) },
            text = { Text(stringResource(R.string.this_action_cannot_be_undone)) }
        )
    }

    if (deleteTriggered) {
        LaunchedEffect(Unit) {
            delay(300)
            onDeleteConfirmed()
        }
    }

    if (showDialogEdit) {
        AlertDialog(
            onDismissRequest = { showDialogEdit = false },
            title = { Text(stringResource(R.string.edit_task)) },
            text = {
                Column {
                    Text(stringResource(R.string.edit_task_title))
                    Spacer(Modifier.height(8.dp))
                    TextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        placeholder = { AnimatedPlaceholder(textFieldValue = editTitle) }
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.select_color))
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
                    Text(stringResource(R.string.select_state))
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
                    Spacer(Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isLockedState = !isLockedState }
                            .padding(vertical = 8.dp)
                    ) {
                        Checkbox(
                            checked = isLockedState,
                            onCheckedChange = { isLockedState = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.lock_this_task_with_fingerprint))
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
                                state = editState,
                                locked = isLockedState
                            )
                        )
                        showDialogEdit = false
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.should_title_be_short_less_than_13_characters),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }) {
                    Text(stringResource(R.string.update))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialogEdit = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

