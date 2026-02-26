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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.ibrahim.to_dolist.MainActivity
import com.ibrahim.to_dolist.data.model.ToDo
import com.ibrahim.to_dolist.presentation.ui.component.TaskSheet
import com.ibrahim.to_dolist.presentation.util.TaskSheetType
import com.ibrahim.to_dolist.util.BiometricHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ToDoListScreen(viewModel: ToDoViewModel, modifier: Modifier, mainActivity: MainActivity ,navController: NavController) {
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

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()
    var showSheet by rememberSaveable { mutableStateOf<ToDo?>(null) }
    LaunchedEffect(selectedToDo) {
        selectedToDo?.let { todo ->
            navController.navigate("tasks/${todo.id}/${todo.cardColor.name}/${todo.title}")
            viewModel.clearSelectedToDo()
        }
    }
    LaunchedEffect(Unit) {
        viewModel.action.collect { action ->
            when (action) {
                is ToDoAction.RequestConfirm -> {
                    when (action.type) {

                        ActionType.DELETE -> showDeleteDialog = action.todo
                        ActionType.EDIT -> showSheet = action.todo
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
                                ActionType.DELETE -> viewModel.onConfirmationAgreed(action.todo, ActionType.DELETE)
                                ActionType.EDIT -> viewModel.onConfirmationAgreed(action.todo, ActionType.EDIT)
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
                    .animateItem(),
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

    // Edit sheet

    showSheet?.let { selectedTodo ->

        ModalBottomSheet(
            onDismissRequest = { showSheet = null },
            sheetState = sheetState
        ) {

            TaskSheet(
                type = TaskSheetType.UPDATE,
                task = selectedTodo,
                onTaskAction = { updatedTodo ->

                    viewModel.updateToDo(updatedTodo)

                    scope.launch {
                        sheetState.hide()
                    }

                    showSheet = null
                }
            )
        }
    }

}
