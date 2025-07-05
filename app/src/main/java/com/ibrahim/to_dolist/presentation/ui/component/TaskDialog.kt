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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
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
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Subtasks for \"$todoTitle\"")
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
                            IconButton(onClick = { onDeleteSubTask(subTask) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete"
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

