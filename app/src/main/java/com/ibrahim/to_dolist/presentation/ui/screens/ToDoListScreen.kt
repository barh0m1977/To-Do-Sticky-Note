package com.ibrahim.to_dolist.presentation.ui.screens

import CardStickyNote
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ibrahim.to_dolist.presentation.ui.component.TaskDialog
import com.ibrahim.to_dolist.presentation.viewmodel.ToDoViewModel
import kotlinx.coroutines.flow.flowOf

/*
    @Author: Ibrahim Lubbad
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToDoListScreen(viewModel: ToDoViewModel) {
    val todos by viewModel.todos.collectAsState()
    val selectedToDo by viewModel.selectedToDo.collectAsState()
    val gridState = rememberLazyGridState()
    //The optimization this  prevents any unnecessary or redundant subscription every time selectedToDo changes.
    val subTasks by remember(selectedToDo?.id) {
        selectedToDo?.let {
            viewModel.getTasksFlow(it.id)
        } ?: flowOf(emptyList())
    }.collectAsState(initial = emptyList())

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(todos.reversed(), key = { it.id }) { todo ->
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
                    viewModel.selectToDo(todo)
                }
            )
        }
    }

    selectedToDo?.let { todo ->
        TaskDialog(
            todoTitle = todo.title,
            todoId = todo.id,
            viewModel = viewModel,
            onAddSubTask = { newText -> viewModel.addTask(todo.id, newText) },
            onUpdateSubTask = { updatedTask -> viewModel.updateTask(updatedTask) },
            onDeleteSubTask = { taskToDelete -> viewModel.deleteTask(taskToDelete) },
            onDismiss = { viewModel.clearSelectedToDo() } // ✅ يغلق الديالوج
        )
    }
}







