package com.ibrahim.to_dolist.presentation.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.ibrahim.to_dolist.core.utility.BiometricHelper
import com.ibrahim.to_dolist.core.utility.toLocalDate
import com.ibrahim.to_dolist.data.model.ToDo
import com.ibrahim.to_dolist.data.model.ToDoState
import com.ibrahim.to_dolist.presentation.ui.component.CardStickyNote
import com.ibrahim.to_dolist.presentation.ui.component.TaskDialog
import com.ibrahim.to_dolist.presentation.viewmodel.ToDoViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.util.concurrent.Executor


@Composable
fun CalendarWithTaskToDo(viewModel: ToDoViewModel) {
    val today = LocalDate.now()
    val yearMonth = YearMonth.now()
    val firstDay = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayIndex = (firstDay.dayOfWeek.value % 7) // Sunday = 0
    val todosModel = viewModel.todos.collectAsState(initial = emptyList())
    val todos = todosModel.value
    // Use epoch day to store LocalDate safely in Compose state
    var selectedDateEpoch by remember { mutableStateOf(today.toEpochDay()) }
    val selectedDate = LocalDate.ofEpochDay(selectedDateEpoch)
    // state to show a dialog
    var showDialog by remember { mutableStateOf(false) }
    var selectedTodo by remember { mutableStateOf<ToDo?>(null) }
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Month title
        Text(
            text = "${yearMonth.month.name} ${yearMonth.year}",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.inversePrimary,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Days of week header
        val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            daysOfWeek.forEach {
                Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar grid
        var dayCounter = 1
        for (week in 0..5) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (dayOfWeek in 0..6) {
                    if (week == 0 && dayOfWeek < firstDayIndex || dayCounter > daysInMonth) {
                        Box(modifier = Modifier.size(40.dp)) {} // empty cell
                    } else {
                        val date = yearMonth.atDay(dayCounter)
                        val todosForDay = todos.filter { it.createdAt.toLocalDate() == date }

                        val backgroundColor by animateColorAsState(
                            if (date.toEpochDay() == selectedDateEpoch) Color(0xFF1E90FF)
                            else if (date == today) Color(0xFFFF6B6B)
                            else Color.White
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(backgroundColor)
                                .clickable { selectedDateEpoch = date.toEpochDay() }
                        ) {
                            Text(
                                text = dayCounter.toString(),
                                fontSize = 14.sp,
                                color = if (date.toEpochDay() == selectedDateEpoch || date == today) Color.White else Color.Black
                            )

                            // small dots for tasks (max 3)
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                todosForDay.take(3).forEach { todo ->
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .padding(1.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (todo.state) {
                                                    ToDoState.DONE -> Color(0xFF4CAF50)
                                                    ToDoState.IN_PROGRESS -> Color(0xFFFFC107)
                                                    else -> Color.Gray
                                                }
                                            )
                                    )
                                }
                            }
                        }
                        dayCounter++
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom panel: tasks for selected date
        val tasks = todos.filter { it.createdAt.toLocalDate() == selectedDate }
        if (tasks.isNotEmpty()) {
            Text(
                text = "Tasks for ${selectedDate.dayOfMonth}/${selectedDate.monthValue}/${selectedDate.year}",
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(columns),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)

            ) {
                items(tasks, key = { it.id }) { todo ->
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
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT)
                                                    .show()
                                            }
                                        ).authenticate()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Activity is null",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }
                                } else {
                                    viewModel.selectToDo(todo)
                                }
                            }
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(top = 16.dp),
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
        } else {
            Text(
                text = "No tasks for this day",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

    }
//    if (showDialog && selectedTodo != null) {
//        TaskDialog(
//            todoTitle = selectedTodo!!.title,
//            todoId = selectedTodo!!.id,
//            viewModel = viewModel,
//            onAddSubTask = { newText -> viewModel.addTask(selectedTodo!!.id, newText) },
//            onUpdateSubTask = { updatedTask -> viewModel.updateTask(updatedTask) },
//            onDeleteSubTask = { taskToDelete -> viewModel.deleteTask(taskToDelete) },
//            onDismiss = {
//                viewModel.clearSelectedToDo()
//                showDialog = false
//            }
//        )
//    }
}
