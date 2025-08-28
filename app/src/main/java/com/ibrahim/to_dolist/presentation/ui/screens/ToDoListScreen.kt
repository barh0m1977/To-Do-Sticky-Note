package com.ibrahim.to_dolist.presentation.ui.screens

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.ibrahim.to_dolist.core.utility.BiometricHelper
import com.ibrahim.to_dolist.data.model.ToDo
import com.ibrahim.to_dolist.presentation.ui.component.CardStickyNote
import com.ibrahim.to_dolist.presentation.ui.component.TaskDialog
import com.ibrahim.to_dolist.presentation.viewmodel.ToDoViewModel
import java.util.concurrent.Executor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToDoListScreen(viewModel: ToDoViewModel, modifier: Modifier) {
    val todos by viewModel.todos.collectAsState()
    val selectedToDo by viewModel.selectedToDo.collectAsState()
    val gridState = rememberLazyGridState()
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val executor: Executor = ContextCompat.getMainExecutor(context)
    var showConfirmDialog by remember { mutableStateOf(false) }
    var targetToDo by remember { mutableStateOf<ToDo?>(null) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val columns = if (screenWidth < 600.dp) 2 else 4

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
                    .clickable {
                        if (todo.locked) {
                            if (activity != null) {
                                BiometricHelper(
                                    activity = activity,
                                    onSuccess = {
                                        viewModel.selectToDo(todo)
                                    },
                                    onError = { msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                ).authenticate()
                            } else {
                                Toast.makeText(context, "Activity is null", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } else {
                            viewModel.selectToDo(todo)
                        }
                    }
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
                            state = updatedToDo.state,
                            locked = updatedToDo.locked
                        )
                    )
                },
                onClick = { },
                isLocked = todo.locked
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

    //    if (showConfirmDialog && targetToDo != null) {
    //        val isLocking = !targetToDo!!.locked
    //        AlertDialog(
    //            onDismissRequest = {
    //                showConfirmDialog = false
    //                targetToDo = null
    //            },
    //            title = {
    //                Text(text = if (isLocking) stringResource(R.string.lock_this_card) else stringResource(
    //                    R.string.unlock_this_card
    //                ))
    //            },
    //            text = {
    //                Text(
    //                    text = if (isLocking)
    //                        stringResource(R.string.do_you_want_to_lock_this_card_with_fingerprint)
    //                    else
    //                        stringResource(R.string.unlock_with_fingerprint)
    //                )
    //            },
    //            confirmButton = {
    //                TextButton(onClick = {
    //                    BiometricHelper(
    //                        activity = activity,
    //                        onSuccess = {
    //                            val updated = targetToDo!!.copy(locked = isLocking)
    //                            viewModel.updateToDo(updated)
    //                            showConfirmDialog = false
    //                            targetToDo = null
    //                        },
    //                        onError = {
    //                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
    //                            showConfirmDialog = false
    //                            targetToDo = null
    //                        }
    //                    ).authenticate()
    //                }) {
    //                    Text(stringResource(R.string.yes))
    //                }
    //            },
    //            dismissButton = {
    //                TextButton(onClick = {
    //                    showConfirmDialog = false
    //                    targetToDo = null
    //                }) {
    //                    Text(stringResource(R.string.no))
    //                }
    //            }
    //        )
    //    }
}

