package com.ibrahim.to_dolist.presentation.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Expand
import androidx.compose.material.icons.filled.Upcoming
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.ibrahim.to_dolist.core.utility.BiometricHelper
import com.ibrahim.to_dolist.core.utility.exportAsIcsWithTasks
import com.ibrahim.to_dolist.core.utility.exportDatabase
import com.ibrahim.to_dolist.core.utility.toLocalDateTime
import com.ibrahim.to_dolist.data.db.ToDoDatabase
import com.ibrahim.to_dolist.data.model.ToDo
import com.ibrahim.to_dolist.data.model.ToDoState
import com.ibrahim.to_dolist.presentation.ui.component.CardStickyNote
import com.ibrahim.to_dolist.presentation.ui.component.TaskDialog
import com.ibrahim.to_dolist.presentation.viewmodel.ToDoViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.YearMonth

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarWithTaskToDo(viewModel: ToDoViewModel = koinViewModel()) {
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
    rememberLazyGridState()

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    if (screenWidth < 600.dp) 2 else 4
    val sizeOfBox = 50
    // timeline
    var timeline by remember { mutableStateOf(false) }
    // export
    var showExportDialog by remember { mutableStateOf(false) }


    // ✅ Helper: check if a task overlaps a given day
    fun ToDo.overlapsDay(day: LocalDate): Boolean {
        val start = this.createdAt.toLocalDateTime()
        val duration = this.durationMinutes ?: 60
        val end = start.plusMinutes(duration.toLong())

        val dayStart = day.atStartOfDay()
        val dayEnd = dayStart.plusDays(1)

        return start.isBefore(dayEnd) && end.isAfter(dayStart)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Month title
            Text(
                text = "${yearMonth.month.name} ${yearMonth.year}",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.inversePrimary,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = {
                  showExportDialog=true
                }) {
                    Icon(
                        Icons.Default.Upcoming,
                        "Export as",
                        modifier = Modifier.padding(horizontal = 5.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
                IconButton(onClick = {
                    timeline = !timeline
                }) {
                    Icon(
                        Icons.Default.Expand,
                        "time line",
                        modifier = Modifier.padding(horizontal = 5.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        // Days of week header
        val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            daysOfWeek.forEach {
                Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(sizeOfBox.dp), textAlign = TextAlign.Center)
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
                        Box(modifier = Modifier.size(sizeOfBox.dp)) {} // empty cell
                    } else {
                        val date = yearMonth.atDay(dayCounter)
                        val todosForDay = todos.filter { it.overlapsDay(date) } // ✅ updated

                        val backgroundColor by animateColorAsState(
                            if (date.toEpochDay() == selectedDateEpoch) MaterialTheme.colorScheme.primary
                            else if (date == today) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.background
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .size(sizeOfBox.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(backgroundColor)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedDateEpoch = date.toEpochDay() }
                        ) {
                            Text(
                                text = dayCounter.toString(),
                                fontSize = 14.sp,
                                color = if (date.toEpochDay() == selectedDateEpoch || date == today) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.primary
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
        val tasks = todos.filter { it.overlapsDay(selectedDate) } // ✅ updated

        if (tasks.isNotEmpty()) {
            Text(
                text = "Tasks for ${selectedDate.dayOfMonth}/${selectedDate.monthValue}/${selectedDate.year}",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (timeline) {
                TaskTimeline(tasks = tasks)
            } else {
                ToDoListScreenForDay(tasks = tasks, viewModel)
            }
        } else {
            Text(
                text = "No tasks for this day",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Tasks") },
            text = { Text("Choose a format to export your tasks:") },
            confirmButton = {
                Column {
                    Text(
                        "Export as Database",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showExportDialog = false
                                exportDatabase(context, ToDoDatabase.DATABASE_NAME)
                            }
                            .padding(8.dp)
                    )
                    Text(
                        "Export as Ics",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showExportDialog = false
                                exportAsIcsWithTasks(context,viewModel.todosWithTasks.value)
                            }
                            .padding(8.dp)
                    )

                }
            },
            dismissButton = {
                Text(
                    "Cancel",
                    modifier = Modifier.clickable { showExportDialog = false }
                )
            }
        )
    }


}

@Composable
fun TaskTimeline(tasks: List<ToDo>) {
    val timelineStartHour = 0
    val timelineEndHour = 23
    val hours = (timelineStartHour..timelineEndHour).toList()
    val hourHeightDp = 60.dp

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(hours) { hour ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(hourHeightDp)
                    .border(0.5.dp, Color.LightGray)
            ) {
                // Hour label
                Text(
                    text = String.format("%02d:00", hour),
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp),
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                // Tasks for this hour
                tasks.filter {
                    val t = it.createdAt.toLocalDateTime()
                    t.hour == hour
                }.forEach { task ->
                    val taskStart = task.createdAt.toLocalDateTime()
                    val taskDuration = task.durationMinutes ?: 60
                    taskStart.plusMinutes(taskDuration.toLong())

                    val startOffset = (taskStart.minute + taskStart.second / 60f) / 60f * hourHeightDp.value
                    val durationHeight = (taskDuration / 60f) * hourHeightDp.value

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(durationHeight.dp)
                            .offset(x = 60.dp, y = startOffset.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(task.cardColor.listColor[2])
                    ) {
                        Text(
                            text = task.title,
                            color = Color.White,
                            modifier = Modifier.padding(4.dp),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ToDoListScreenForDay(tasks: List<ToDo>, viewModel: ToDoViewModel) {
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val selectedToDo by viewModel.selectedToDo.collectAsState()
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(tasks, key = { it.id }) { todo ->
            CardStickyNote(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(top = 16.dp)
                    .clickable {
                        if (todo.locked) {
                            BiometricHelper(
                                activity = context as FragmentActivity,
                                onSuccess = { viewModel.selectToDo(todo) },
                                onError = { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            ).authenticate()
                        } else {
                            viewModel.selectToDo(todo)
                        }
                    },
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

}


