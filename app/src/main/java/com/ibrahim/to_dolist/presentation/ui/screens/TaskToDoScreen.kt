package com.ibrahim.to_dolist.presentation.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ibrahim.to_dolist.R
import com.ibrahim.to_dolist.animation.AnimatedPlaceholder
import com.ibrahim.to_dolist.data.model.Tasks
import com.ibrahim.to_dolist.data.model.ToDoStickyColors
import com.ibrahim.to_dolist.presentation.ui.component.cardStyle.ToDoCard
import com.ibrahim.to_dolist.presentation.ui.screens.todolist.ToDoViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

// ─── Colors ───────────────────────────────────────────────────────────────────

private val LightGreen = Color(0xFFE0F7FA)
private val DarkGreen = Color(0xFF00C853)
private val DarkGray = Color(0xFF9E9E9E)
private val BackgroundColor = Color(0xFFF5F5F5)

// ─── Swipe thresholds ────────────────────────────────────────────────────────

private const val SWIPE_ACTION_THRESHOLD_DP = 80
private const val SWIPE_MAX_DP = 120

// ─── Animation specs ──────────────────────────────────────────────────────────

private val TaskEnterTransition = fadeIn(tween(400)) + slideInVertically(
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioHighBouncy,
        stiffness = Spring.StiffnessVeryLow
    ),
    initialOffsetY = { it / 2 },
)

private val TaskExitTransition = fadeOut(tween(300)) + slideOutVertically(
    animationSpec = tween(300),
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

    val accentColor = remember(cardColor) {
        ToDoStickyColors.valueOf(cardColor).listColor[1]
    }

    var searchQuery by rememberSaveable { mutableStateOf("") }

    val (activeTasks, completedTasks) = remember(tasks, searchQuery) {
        val active = tasks.filter { !it.isChecked }
        val completed = tasks.filter { it.isChecked }

        if (searchQuery.isBlank()) {
            active to completed
        } else {
            val query = searchQuery
            active.filter { it.text.contains(query, ignoreCase = true) } to
                    completed.filter { it.text.contains(query, ignoreCase = true) }
        }
    }

    // ── Dialog state for delete confirmation ────────────────────────────────
    var taskToDelete by remember { mutableStateOf<Tasks?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }  // ← New state
    var editingTask by remember { mutableStateOf<Tasks?>(null) }
    val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                onClick = { showAddSheet = true },
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
    ) { paddingValues ->
        TaskListContent(
            paddingValues = paddingValues,
            activeTasks = activeTasks,
            completedTasks = completedTasks,
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            accentColor = accentColor,
            onDeleteTask = { taskToDelete = it },
            onEditTask = { editingTask = it },
            onClearAll = { showClearAllDialog = true },  // ← New handler
            onCompleteTask = { task -> viewModel.updateTask(task.copy(isChecked = true)) },
            onUncompleteTask = { task -> viewModel.updateTask(task.copy(isChecked = false)) },
        )
    }

    // ── Add Task Sheet ──────────────────────────────────────────────────────
    if (showAddSheet) {
        AddTaskBottomSheet(
            sheetState = addSheetState,
            accentColor = accentColor,
            onDismiss = { showAddSheet = false },
            onConfirm = { text ->
                if (text.isNotBlank()) {
                    viewModel.addTask(todoId = todoId, text = text.trim())
                }
                scope.launch {
                    addSheetState.hide()
                }.invokeOnCompletion {
                    showAddSheet = false
                }
            },
        )
    }

    // ── Delete Single Task Dialog ───────────────────────────────────────────
    taskToDelete?.let { task ->
        DeleteConfirmationDialog(
            taskTitle = task.text,
            accentColor = accentColor,
            onConfirm = {
                viewModel.deleteTask(task)
                taskToDelete = null
            },
            onDismiss = { taskToDelete = null },
        )
    }

    // ── Clear All Completed Tasks Dialog ────────────────────────────────────
    if (showClearAllDialog) {
        ClearAllConfirmationDialog(
            count = completedTasks.size,
            accentColor = accentColor,
            onConfirm = {
                completedTasks.forEach { viewModel.deleteTask(it) }
                showClearAllDialog = false
            },
            onDismiss = { showClearAllDialog = false },
        )
    }

    // ── Edit Task Sheet ─────────────────────────────────────────────────────
    editingTask?.let { task ->
        EditTaskBottomSheet(
            sheetState = editSheetState,
            accentColor = accentColor,
            initialText = task.text,
            onDismiss = { editingTask = null },
            onConfirm = { newText ->
                if (newText.isNotBlank()) {
                    viewModel.updateTask(task.copy(text = newText.trim()))
                }
                scope.launch {
                    editSheetState.hide()
                }.invokeOnCompletion {
                    editingTask = null
                }
            },
        )
    }
}

// ─── TaskListContent ──────────────────────────────────────────────────────────

@Composable
private fun TaskListContent(
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    activeTasks: List<Tasks>,
    completedTasks: List<Tasks>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    accentColor: Color,
    onDeleteTask: (Tasks) -> Unit,
    onEditTask: (Tasks) -> Unit,
    onClearAll: () -> Unit,  // ← New callback
    onCompleteTask: (Tasks) -> Unit,
    onUncompleteTask: (Tasks) -> Unit,
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            TaskSearchBar(
                query = searchQuery,
                onQueryChange = onSearchChange,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(28.dp))
        }

        item {
            TaskSectionHeader(
                label = stringResource(R.string.active_tasks),
                badge = "${activeTasks.size} tasks",
                badgeBackground = accentColor.copy(alpha = 0.1f),
                badgeTextColor = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(items = activeTasks, key = { it.id }) { task ->
            AnimatedVisibility(
                visible = true,
                enter = TaskEnterTransition,
                exit = TaskExitTransition,
            ) {
                SwipeableTaskCard(
                    accentColor = accentColor,
                    onDelete = { onDeleteTask(task) },
                    onEdit = { onEditTask(task) },
                ) {
                    ToDoCard(
                        title = task.text,
                        subtitle = "",
                        accentColor = accentColor,
                        isCompleted = false,
                        onCheckedChange = { onCompleteTask(task) },
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            TaskSectionHeader(
                label = stringResource(R.string.completed),
                trailingAction = {
                    if (completedTasks.isNotEmpty()) {
                        TextButton(
                            onClick = { onClearAll() },  // ← Call new callback
                        ) {
                            Text(stringResource(R.string.clear_all), color = DarkGray, fontSize = 12.sp)
                        }
                    }
                },
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(items = completedTasks, key = { it.id }) { task ->
            AnimatedVisibility(
                visible = true,
                enter = TaskEnterTransition,
                exit = TaskExitTransition,
            ) {
                SwipeableTaskCard(
                    accentColor = accentColor,
                    onDelete = { onDeleteTask(task) },
                    onEdit = { onEditTask(task) },
                ) {
                    ToDoCard(
                        title = task.text,
                        subtitle = "",
                        accentColor = accentColor,
                        isCompleted = true,
                        onCheckedChange = { onUncompleteTask(task) },
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

// ─── SwipeableTaskCard ────────────────────────────────────────────────────────

@Composable
private fun SwipeableTaskCard(
    accentColor: Color,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val thresholdPx = with(density) { SWIPE_ACTION_THRESHOLD_DP.dp.toPx() }
    val maxPx = with(density) { SWIPE_MAX_DP.dp.toPx() }

    val offsetX = remember { Animatable(0f) }
    var didPassThreshold by remember { mutableStateOf(false) }

    val snapSpec: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val draggableState = rememberDraggableState { delta ->
        scope.launch {
            val target = (offsetX.value + delta).coerceIn(-maxPx, maxPx)
            offsetX.snapTo(target)

            val crossed = abs(target) >= thresholdPx
            if (crossed != didPassThreshold) {
                if (crossed) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                didPassThreshold = crossed
            }
        }
    }

    val swipeFraction = (abs(offsetX.value) / thresholdPx).coerceIn(0f, 1f)
    val isSwipingRight = offsetX.value > 0f

    val deleteColor = lerp(
        accentColor.copy(alpha = 0.7f),
        accentColor,
        swipeFraction
    )
    val editColor = lerp(
        accentColor.copy(alpha = 0.5f),
        accentColor,
        swipeFraction
    )

    val iconScale by animateFloatAsState(
        targetValue = if (didPassThreshold) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "iconScale"
    )

    val showBackground = abs(offsetX.value) > 2f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(16.dp))
    ) {
        if (showBackground) {
            SwipeBackground(
                color = if (isSwipingRight) deleteColor else editColor,
                icon = if (isSwipingRight) Icons.Default.Delete else Icons.Default.Edit,
                label = if (isSwipingRight) "Delete" else "Edit",
                iconColor = Color.White,
                iconScale = iconScale,
                alignment = if (isSwipingRight) Alignment.CenterStart else Alignment.CenterEnd,
                fraction = swipeFraction,
            )
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .fillMaxWidth()
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        scope.launch {
                            if (abs(offsetX.value) >= thresholdPx) {
                                if (offsetX.value > 0f) onDelete() else onEdit()
                            }
                            didPassThreshold = false
                            offsetX.animateTo(0f, snapSpec)
                        }
                    }
                )
        ) {
            content()
        }
    }
}

// ─── SwipeBackground ─────────────────────────────────────────────────────────

@Composable
private fun SwipeBackground(
    color: Color,
    icon: ImageVector,
    label: String,
    iconColor: Color,
    iconScale: Float,
    alignment: Alignment,
    fraction: Float,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(color, RoundedCornerShape(16.dp)),
        contentAlignment = alignment,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .scale(iconScale),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = iconColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ─── Delete Single Task Dialog ────────────────────────────────────────────────

@Composable
private fun DeleteConfirmationDialog(
    taskTitle: String,
    accentColor: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val title = taskTitle.split(" ")[0]

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.delete_task),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                Text(
                    text = "\"$title...\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.this_action_cannot_be_undone),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
            ) {
                Text(stringResource(R.string.delete), color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.primary)
            }
        },
    )
}

// ─── Clear All Confirmation Dialog ──────────────────────────────────────────

/**
 * Dialog to confirm clearing all completed tasks at once.
 * Shows count of tasks to be deleted.
 */
@Composable
private fun ClearAllConfirmationDialog(
    count: Int,
    accentColor: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.clear_all),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                Text(
                    text = "You are about to delete $count completed task${if (count > 1) "s" else ""}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.this_action_cannot_be_undone),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
            ) {
                Text(stringResource(R.string.clear_all), color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.primary)
            }
        },
    )
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
        modifier = Modifier.padding(top = 12.dp),
    )
}

// ─── Search Bar ───────────────────────────────────────────────────────────────

@Composable
private fun TaskSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    color: Color
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(R.string.search_tasks)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(1.dp, color, RoundedCornerShape(12)),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = color.copy(alpha = 0.3f),
            unfocusedContainerColor = color.copy(alpha = 0.2f),
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

    val submit = {
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
                text =stringResource(R.string.new_task),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            OutlinedTextField(
                value = taskText,
                onValueChange = { taskText = it },
                placeholder = { AnimatedPlaceholder(taskText) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor),
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
                    onClick = { submit() },
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

// ─── Edit Task Bottom Sheet ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTaskBottomSheet(
    sheetState: SheetState,
    accentColor: Color,
    initialText: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var taskText by remember(initialText) { mutableStateOf(initialText) }
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    val submit = {
        keyboard?.hide()
        onConfirm(taskText)
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(accentColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.edit_task),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            OutlinedTextField(
                value = taskText,
                onValueChange = { taskText = it },
                placeholder = { Text("Task text…") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor),
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
                    onClick = { submit() },
                    enabled = taskText.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                ) {
                    Text(stringResource(R.string.save), color = Color.White)
                }
            }
        }
    }
}