package com.ibrahim.to_dolist.presentation.ui.component.dialog

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ibrahim.to_dolist.R
import com.ibrahim.to_dolist.data.model.ToDo
import com.ibrahim.to_dolist.data.model.ToDoState
import com.ibrahim.to_dolist.data.model.ToDoStickyColors
import com.ibrahim.to_dolist.presentation.ui.component.ColorCircle
import com.ibrahim.to_dolist.presentation.ui.component.ToDoStateLabel
import com.ibrahim.to_dolist.presentation.ui.screens.AnimatedPlaceholder
import com.ibrahim.to_dolist.presentation.ui.screens.isLeesThan

@Composable
fun TaskEditDialog(
    todo: ToDo,
    onUpdate: (ToDo) -> Unit,
    onDismiss: () -> Unit
) {
    var editTitle by rememberSaveable { mutableStateOf(todo.title) }
    var editColor by rememberSaveable { mutableStateOf(todo.cardColor) }
    var editState by rememberSaveable { mutableStateOf(todo.state) }
    var isLockedState by rememberSaveable { mutableStateOf(todo.locked) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { onDismiss() },
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
                        ColorCircle(color.listColor[0], isSelected = color == editColor) {
                            editColor = color
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.select_state))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ToDoState.entries.size) { index ->
                        val s = ToDoState.entries[index]
                        ToDoStateLabel(state = s, isSelected = s == editState, onClick = { editState = it })
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
                    Checkbox(checked = isLockedState, onCheckedChange = { isLockedState = it })
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.lock_this_task_with_fingerprint))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (isLeesThan(editTitle)) {
                    onUpdate(
                        todo.copy(
                            title = editTitle,
                            cardColor = editColor,
                            state = editState,
                            locked = isLockedState
                        )
                    )
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.should_title_be_short_less_than_13_characters),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }) { Text(stringResource(R.string.update)) }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text(stringResource(R.string.cancel)) }
        }
    )
}
