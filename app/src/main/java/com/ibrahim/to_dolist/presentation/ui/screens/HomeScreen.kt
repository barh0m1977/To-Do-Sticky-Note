package com.ibrahim.to_dolist.presentation.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ibrahim.to_dolist.data.model.ToDo
import com.ibrahim.to_dolist.data.model.ToDoState
import com.ibrahim.to_dolist.data.model.ToDoStickyColors
import com.ibrahim.to_dolist.presentation.ui.component.ColorCircle
import com.ibrahim.to_dolist.presentation.ui.component.ToDoStateLabel
import com.ibrahim.to_dolist.presentation.util.SortOption
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToDoTopBar(
    selectedSortOption: SortOption,
    onSortOptionChanged: (SortOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("To-Do List") },
        actions = {
            Box {
                TextButton(onClick = { expanded = true }) {
                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort Icon")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        when (selectedSortOption) {
                            SortOption.CREATED_DATE -> "Created"
                            SortOption.MODIFIED_DATE -> "Modified"
                            SortOption.ONLY_DONE -> "Only Done"
                            SortOption.ONLY_PENDING -> "Only Pending"
                            SortOption.ONLY_IN_PROGRESS -> "Only In Progress"
                            SortOption.OPENED -> "Opened"
                            SortOption.LOCKED -> "Locked"
                        }
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Sort by Created Date") },
                        onClick = {
                            onSortOptionChanged(SortOption.CREATED_DATE)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Sort by Modified Date") },
                        onClick = {
                            onSortOptionChanged(SortOption.MODIFIED_DATE)
                            expanded = false
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Show Only DONE") },
                        onClick = {
                            onSortOptionChanged(SortOption.ONLY_DONE)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Show Only PENDING") },
                        onClick = {
                            onSortOptionChanged(SortOption.ONLY_PENDING)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Show Only IN_PROGRESS") },
                        onClick = {
                            onSortOptionChanged(SortOption.ONLY_IN_PROGRESS)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Show Opened") },
                        onClick = {
                            onSortOptionChanged(SortOption.OPENED)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Show Locked") },
                        onClick = {
                            onSortOptionChanged(SortOption.LOCKED)
                            expanded = false
                        }
                    )
                }

            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: ToDoViewModel) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var text by rememberSaveable { mutableStateOf("") }
    var colorVal by rememberSaveable { mutableStateOf(ToDoStickyColors.SUNRISE) }
    var selectedColor by rememberSaveable { mutableStateOf(ToDoStickyColors.SUNRISE) }
    var selectedState by rememberSaveable { mutableStateOf(ToDoState.PENDING) }
    var isLocked by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            ToDoTopBar(
                selectedSortOption = viewModel.sortOption,
                onSortOptionChanged = viewModel::onSortOptionChanged
            )
        },
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
            ToDoListScreen(viewModel)

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

                            Spacer(Modifier.height(8.dp))
                            Text("Select color:")
                            Spacer(Modifier.height(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                            ) {
                                items(ToDoStickyColors.entries.size) { index ->
                                    val color = ToDoStickyColors.entries[index]
                                    ColorCircle(
                                        color = color.listColor[0],
                                        isSelected = color == selectedColor
                                    ) {
                                        selectedColor = color
                                        colorVal = color
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            Text("Select State:")
                            Spacer(Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(ToDoState.entries.size) { index ->
                                    val state = ToDoState.entries[index]
                                    ToDoStateLabel(
                                        state = state,
                                        isSelected = state == selectedState
                                    ) {
                                        selectedState = it
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isLocked = !isLocked }
                                    .padding(vertical = 8.dp)
                            ) {
                                Checkbox(
                                    checked = isLocked,
                                    onCheckedChange = { isLocked = it }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Lock this task with fingerprint")
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (isLeesThan(text)) {
                                viewModel.addToDo(
                                    ToDo(
                                        title = text,
                                        cardColor = colorVal,
                                        state = selectedState,
                                        locked = isLocked
                                    )
                                )
                                showDialog = false
                                text = ""
                            } else {
                                Toast.makeText(
                                    context,
                                    "should title be short \n less than 13 characters",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDialog = false
                            text = ""
                            isLocked = false
                        }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

fun isLeesThan(text: String): Boolean {
    return text.isNotEmpty() && text.isNotBlank() && text.length <= 13

}
