package com.ibrahim.to_dolist.presentation.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ibrahim.to_dolist.data.model.ToDo
import com.ibrahim.to_dolist.data.model.ToDoState
import com.ibrahim.to_dolist.data.model.ToDoStickyColors
import com.ibrahim.to_dolist.presentation.viewmodel.ToDoViewModel
import kotlinx.coroutines.delay

@Composable
fun AnimatedPlaceholder(textFieldValue: String) {
    val fullText = "task text here..."
    var visibleText by remember { mutableStateOf("") }

    LaunchedEffect(key1 = textFieldValue.isEmpty()) {
        while (textFieldValue.isEmpty()) {
            for (i in 1..fullText.length) {
                visibleText = fullText.take(i)
                delay(100)
            }
            delay(500)
            visibleText = ""
            delay(300)
        }
    }

    if (textFieldValue.isEmpty()) {
        Text(visibleText, color = Color.Gray)
    }
}
@Composable
fun HomeScreen(viewModel: ToDoViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // 📝 Display ToDo list
            ToDoListScreen(viewModel)

            // 🗨️ Show Add Dialog
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("New Task") },
                    text = {
                        Column {
                            Text("Enter task title:")
                            Spacer(Modifier.height(8.dp))
                            TextField(
                                value = text,
                                onValueChange = { text = it },
                                placeholder = {
                                    AnimatedPlaceholder(textFieldValue = text)
                                }

                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (text.isNotBlank()) {
                                viewModel.addToDo(
                                    ToDo(title = text, cardColor = ToDoStickyColors.MINT_CREAM, state = ToDoState.IN_PROGRESS)
                                )
                            }
                            text = ""
                            showDialog = false
                        }) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDialog = false
                            text = ""
                        }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }



}
