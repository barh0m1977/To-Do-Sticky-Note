package com.ibrahim.to_dolist.presentation.ui.screens

import CardStickyNote
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ibrahim.to_dolist.data.model.ToDo
import com.ibrahim.to_dolist.presentation.viewmodel.ToDoViewModel
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.collectAsState
import com.ibrahim.to_dolist.data.model.Tasks
import com.ibrahim.to_dolist.presentation.ui.component.TaskDialog


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToDoListScreen(viewModel: ToDoViewModel) {
    val todos by viewModel.todos.collectAsState()

    var selectedToDo by remember { mutableStateOf<ToDo?>(null) }
    var showTaskDialog by remember { mutableStateOf(false) }

    val gridState = rememberLazyGridState()

    // جمع مهام ToDo المحدد مباشرة من Flow مع حالة افتراضية فارغة
    val subTasks by selectedToDo?.let { viewModel.getTasksFlow(it.id).collectAsState(initial = emptyList()) }
        ?: remember { mutableStateOf(emptyList()) }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(todos, key = { it.id }) { todo ->
            CardStickyNote(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(top = 16.dp)
                    .animateItemPlacement(),
                text = todo.title,
                colorArray = todo.cardColor,
                state = todo.state,
                onDeleteConfirmed = { viewModel.deleteToDoLocal(todo) },
                onEditConfirmed = { updatedToDo ->
                    viewModel.updateToDo(
                        todo.copy(
                            title = updatedToDo.title,
                            cardColor = updatedToDo.cardColor,
                            state = updatedToDo.state
                        )
                    )
                },
                onClick = {
                    selectedToDo = todo
                    showTaskDialog = true
                }
            )
        }
    }

    if (showTaskDialog && selectedToDo != null) {
        TaskDialog(
            todoTitle = selectedToDo!!.title,
            todoId = selectedToDo!!.id,
            viewModel = viewModel,
            onAddSubTask = { newText -> viewModel.addTask(selectedToDo!!.id, newText) },
            onUpdateSubTask = { updatedTask -> viewModel.updateTask(updatedTask) },
            onDeleteSubTask = { taskToDelete -> viewModel.deleteTask(taskToDelete) },
            onDismiss = {
                showTaskDialog = false
                selectedToDo = null
            }
        )
    }
}








