package com.ibrahim.to_dolist.presentation.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.ibrahim.to_dolist.R
import com.ibrahim.to_dolist.data.model.Tasks
import com.ibrahim.to_dolist.presentation.ui.screens.todolist.ToDoViewModel

@Composable
fun TaskDialog(
    todoTitle: String,
    todoId: Int,
    viewModel: ToDoViewModel,
    onAddSubTask: (String) -> Unit,
    onUpdateSubTask: (Tasks) -> Unit,
    onDeleteSubTask: (Tasks) -> Unit,
    onDismiss: () -> Unit
) {
    val subTasks by viewModel.getTasksFlow(todoId).collectAsState(initial = emptyList())
    var newTaskText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    var showDialogDelete by rememberSaveable { mutableStateOf(false) }
    var subTaskToDelete by remember { mutableStateOf<Tasks?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.subtasks_for, todoTitle))
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .verticalScroll(scrollState)
                ) {
                    subTasks.asReversed().forEach { subTask ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = subTask.isChecked,
                                onCheckedChange = {
                                    onUpdateSubTask(subTask.copy(isChecked = it))
                                }
                            )
                            Text(
                                text = subTask.text,
                                style = if (subTask.isChecked) {
                                    MaterialTheme.typography.bodyLarge.copy(
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                } else {
                                    MaterialTheme.typography.bodyLarge
                                },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                subTaskToDelete = subTask
                                showDialogDelete = true
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.delete)
                                )
                            }
                        }
                        HorizontalDivider(
                            color = Color.LightGray,
                            thickness = 1.dp,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                        )
                    }
                }

                if (scrollState.value < scrollState.maxValue && scrollState.maxValue > 0) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Scroll down",
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(24.dp)
                            .padding(bottom = 1.dp),
                        tint = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = newTaskText,
                    onValueChange = { newTaskText = it },
                    label = { Text(stringResource(R.string.new_subtask)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newTaskText.isNotBlank()) {
                        onAddSubTask(newTaskText)
                        newTaskText = ""
                    }
                }
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )

    // Delete confirmation dialog
    if (showDialogDelete && subTaskToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDialogDelete = false
                subTaskToDelete = null
            },
            confirmButton = {
                TextButton(onClick = {
                    subTaskToDelete?.let { onDeleteSubTask(it) }
                    showDialogDelete = false
                    subTaskToDelete = null
                }) {
                    Text(stringResource(R.string.confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialogDelete = false
                    subTaskToDelete = null
                }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = { Text(stringResource(R.string.are_you_sure)) },
            text = { Text(stringResource(R.string.this_action_cannot_be_undone)) }
        )
    }
}
