package com.ibrahim.to_dolist.presentation.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ibrahim.to_dolist.R
import com.ibrahim.to_dolist.data.model.ToDoStickyColors
import com.ibrahim.to_dolist.presentation.ui.component.cardStyle.ToDoCard
import com.ibrahim.to_dolist.presentation.ui.screens.todolist.ToDoViewModel
import kotlinx.coroutines.launch

// ─── Colors ───────────────────────────────────────────────────────────────────

private val LightGreen = Color(0xFFE0F7FA)
private val DarkGreen = Color(0xFF00C853)
private val DarkGray = Color(0xFF9E9E9E)
private val BackgroundColor = Color(0xFFF0F4F8)

// ─── Animation specs (top-level = allocated once, never recreated on recompose) ──

private val TaskEnterTransition = fadeIn(tween(500)) + slideInVertically(
    animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy ,stiffness = Spring.StiffnessVeryLow),
    initialOffsetY = { it / 2 },
)
private val TaskExitTransition = fadeOut(tween(500)) + slideOutVertically(
    animationSpec = tween(500),
    targetOffsetY = { -it / 2 },
)

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: ToDoViewModel,
    todoId: Int,
    cardColor: String,
    todoTitle: String = "My Tasks",
    onNavigateBack: () -> Unit,
) {
    val tasks by viewModel.getTasksFlow(todoId).collectAsStateWithLifecycle(emptyList())
    val scope = rememberCoroutineScope()

    // Derived — no extra state, no stale data
    val activeTasks = tasks.filter { !it.isChecked }
    val completedTasks = tasks.filter { it.isChecked }

    val accentColor = remember(cardColor) {
        ToDoStickyColors.valueOf(cardColor).listColor[1]
    }

    // Search state hoisted to screen level so it can filter tasks
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val filteredActive = remember(activeTasks, searchQuery) {
        if (searchQuery.isBlank()) activeTasks
        else activeTasks.filter { it.text.contains(searchQuery, ignoreCase = true) }
    }
    val filteredCompleted = remember(completedTasks, searchQuery) {
        if (searchQuery.isBlank()) completedTasks
        else completedTasks.filter { it.text.contains(searchQuery, ignoreCase = true) }
    }

    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TaskTopBar(
                title = todoTitle,
                accentColor = accentColor,
                onNavigateBack = onNavigateBack,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomSheet = true },
                containerColor = accentColor,
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add task",
                    tint = Color.White,
                )
            }
        },
        containerColor = BackgroundColor,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                TaskSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Active tasks ─────────────────────────────────────────────
            item {
                TaskSectionHeader(
                    label = "ACTIVE TASKS",
                    badge = "${filteredActive.size} tasks",
                    badgeBackground = LightGreen,
                    badgeTextColor = DarkGreen,
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(items = filteredActive, key = { it.id }) { task ->
                AnimatedVisibility(
                    visible = true,
                    enter = TaskEnterTransition,
                    exit = TaskExitTransition,
                ) {
                    ToDoCard(
                        title = task.text,
                        subtitle = "",
                        accentColor = accentColor,
                        isCompleted = false,
                        onCheckedChange = { viewModel.updateTask(task.copy(isChecked = true)) },
                    )
                }
            }

            // ── Completed tasks ──────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(20.dp))
                TaskSectionHeader(
                    label = "COMPLETED",
                    trailingAction = {
                        if (filteredCompleted.isNotEmpty()) {
                            TextButton(
                                onClick = { filteredCompleted.forEach { viewModel.deleteTask(it) } },
                            ) {
                                Text("Clear all", color = DarkGray, fontSize = 12.sp)
                            }
                        }
                    },
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(items = filteredCompleted, key = { it.id }) { task ->
                AnimatedVisibility(
                    visible = true,
                    enter = TaskEnterTransition,
                    exit = TaskExitTransition,
                ) {
                    ToDoCard(
                        title = task.text,
                        subtitle = "",
                        accentColor = accentColor,
                        isCompleted = true,
                        onCheckedChange = { viewModel.updateTask(task.copy(isChecked = false)) },
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) } // FAB clearance
        }
    }

    // ── Add Task Bottom Sheet ─────────────────────────────────────────────────
    if (showBottomSheet) {
        AddTaskBottomSheet(
            sheetState = sheetState,
            accentColor = accentColor,
            onDismiss = { showBottomSheet = false },
            onConfirm = { text ->
                if (text.isNotBlank()) {
                    viewModel.addTask(todoId = todoId, text = text.trim())
                }
                scope.launch {
                    sheetState.hide()
                }.invokeOnCompletion {
                    showBottomSheet = false
                }
            },
        )
    }
}

// ─── TopBar ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskTopBar(
    title: String,
    accentColor: Color,
    onNavigateBack: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate back",
                    tint = accentColor,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = BackgroundColor,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = Modifier.padding(top = 18.dp),
    )
}

// ─── Search Bar ───────────────────────────────────────────────────────────────

@Composable
private fun TaskSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search tasks...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
        ),
    )
}

// ─── Section Header ───────────────────────────────────────────────────────────

@Composable
private fun TaskSectionHeader(
    label: String,
    badge: String? = null,
    badgeBackground: Color = Color.Transparent,
    badgeTextColor: Color = Color.Unspecified,
    trailingAction: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = DarkGray,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
        )

        if (badge != null) {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = badgeBackground),
            ) {
                Text(
                    text = badge,
                    color = badgeTextColor,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }

        trailingAction?.invoke()
    }
}

// ─── Add Task Bottom Sheet ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTaskBottomSheet(
    sheetState: SheetState,
    accentColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var taskText by remember { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    val submitTask = {
        keyboard?.hide()
        onConfirm(taskText)
        taskText = ""
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "New Task",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            OutlinedTextField(
                value = taskText,
                onValueChange = { taskText = it },
                placeholder = { Text("What needs to be done?") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { submitTask() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                ),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel), color = DarkGray)
                }

                Button(
                    onClick = { submitTask() },
                    enabled = taskText.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                ) {
                    Text(stringResource(R.string.add_task), color = Color.White)
                }
            }
        }
    }
}