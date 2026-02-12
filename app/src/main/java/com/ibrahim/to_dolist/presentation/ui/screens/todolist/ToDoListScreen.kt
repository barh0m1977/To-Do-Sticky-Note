package com.ibrahim.to_dolist.presentation.ui.screens.todolist

import CardStickyNote
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.ibrahim.to_dolist.MainActivity
import com.ibrahim.to_dolist.data.model.ToDo
import com.ibrahim.to_dolist.presentation.ui.component.TaskDialog
import com.ibrahim.to_dolist.presentation.ui.component.dialog.TaskEditDialog
import com.ibrahim.to_dolist.util.BiometricHelper

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToDoListScreen(viewModel: ToDoViewModel, modifier: Modifier, mainActivity: MainActivity) {
    val todos by viewModel.todos.collectAsState()
    val selectedToDo by viewModel.selectedToDo.collectAsState()
    val gridState = rememberLazyGridState()
    val context = LocalContext.current

    val executor = ContextCompat.getMainExecutor(context)

    var showConfirmDialog by remember { mutableStateOf(false) }
    var targetToDo by remember { mutableStateOf<ToDo?>(null) }

    var showDialogDelete by rememberSaveable { mutableStateOf(false) }
    var showDialogEdit by rememberSaveable { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val columns = if (screenWidth < 600.dp) 2 else 4

    var showDeleteDialog by remember { mutableStateOf<ToDo?>(null) }
    var showEditDialog by remember { mutableStateOf<ToDo?>(null) }
    LaunchedEffect(Unit) {
        viewModel.action.collect { action ->
            when (action) {
                is ToDoAction.RequestConfirm -> {
                    when (action.type) {

                        ActionType.DELETE -> showDeleteDialog = action.todo
                        ActionType.EDIT -> showEditDialog = action.todo
                        else -> {}
                    }
                }
                is ToDoAction.OpenTodo -> {
                    viewModel.openAfterBiometric(action.todo)
                }
                is ToDoAction.RequestBiometric -> {

                    BiometricHelper(
                        context = mainActivity,
                        onSuccess = {
                            when(action.afterSuccess){
                                ActionType.OPEN -> viewModel.openAfterBiometric(action.todo)
                                ActionType.DELETE -> viewModel.deleteAfterBiometric(action.todo)
                                ActionType.EDIT -> viewModel.editeAfterBiometric(action.todo)
                            }
                        },
                        onError = { Toast.makeText(mainActivity, it, Toast.LENGTH_SHORT).show() }
                    ).authenticate()
                }

                is ToDoAction.ShowMessage -> Toast.makeText(
                    context,
                    action.message,
                    Toast.LENGTH_SHORT
                ).show()

                else -> {}
            }
        }
    }


    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(columns),
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
                    .clickable {
                        viewModel.onTodoClicked(todo)
                    }
                    .animateItemPlacement(),
                text = todo.title,
                colorArray = todo.cardColor,
                state = todo.state,
                isLocked = todo.locked,
                onClick = { viewModel.onTodoClicked(todo) },
                onDeleteConfirmed = { viewModel.requestDelete(todo) },
                onEditConfirmed = { viewModel.requestEdit(todo) }
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
            onDismiss = { viewModel.clearSelectedToDo() }
        )
    }


    // Delete Dialog
    showDeleteDialog?.let { todo ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Are you sure?") },
            text = { Text("This action cannot be undone") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAfterBiometric(todo)
                    showDeleteDialog = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") }
            }
        )
    }

    // Edit Dialog
    showEditDialog?.let { todo ->
        TaskEditDialog(
            todo = todo,
            onUpdate = { updated -> viewModel.updateToDo(updated); showEditDialog = null },
            onDismiss = { showEditDialog = null }
        )
    }


}
