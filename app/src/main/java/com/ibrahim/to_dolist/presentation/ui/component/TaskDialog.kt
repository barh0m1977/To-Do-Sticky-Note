package com.ibrahim.to_dolist.presentation.ui.component

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ibrahim.to_dolist.data.model.Tasks
import com.ibrahim.to_dolist.presentation.viewmodel.ToDoViewModel

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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Subtasks for \"$todoTitle\"")
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                subTasks.forEach { subTask ->
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
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onDeleteSubTask(subTask) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = newTaskText,
                    onValueChange = { newTaskText = it },
                    label = { Text("New Subtask") },
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
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

