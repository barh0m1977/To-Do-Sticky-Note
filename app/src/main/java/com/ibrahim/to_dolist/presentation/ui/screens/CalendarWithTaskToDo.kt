package com.ibrahim.to_dolist.presentation.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.YearMonth

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarWithTaskToDo(viewModel: ToDoViewModel = koinViewModel()) {
    val today = LocalDate.now()
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    val yearMonth = currentYearMonth
    val firstDay = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayIndex = (firstDay.dayOfWeek.value % 7)
    val todos by viewModel.todos.collectAsState(initial = emptyList())

    var selectedDateEpoch by remember { mutableStateOf(today.toEpochDay()) }
    val selectedDate = LocalDate.ofEpochDay(selectedDateEpoch)

    var timeline by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var expandedWeekIndex by remember { mutableStateOf<Int?>(null) }

    val context = LocalContext.current
    val sizeOfBox = 50
    val totalWeeks = 6
    val coroutineScope = rememberCoroutineScope()
    val lazyColumnState = rememberLazyListState()

    fun ToDo.overlapsDay(day: LocalDate): Boolean {
        val start = this.createdAt.toLocalDateTime()
        val duration = this.durationMinutes ?: 60
        val end = start.plusMinutes(duration.toLong())
        val dayStart = day.atStartOfDay()
        val dayEnd = dayStart.plusDays(1)
        return start.isBefore(dayEnd) && end.isAfter(dayStart)
    }

    fun LocalDate.weekOfMonth(): Int = ((this.dayOfMonth + firstDayIndex - 1) / 7)

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        // Header: Month navigation + export + expand
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Month navigation
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    currentYearMonth = currentYearMonth.minusMonths(1)
                    selectedDateEpoch = currentYearMonth.atDay(1).toEpochDay()
                    expandedWeekIndex = null
                }) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Prev Month")
                }

                Text(
                    text = "${yearMonth.month.name} ${yearMonth.year}",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.inversePrimary,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                IconButton(onClick = {
                    currentYearMonth = currentYearMonth.plusMonths(1)
                    selectedDateEpoch = currentYearMonth.atDay(1).toEpochDay()
                    expandedWeekIndex = null
                }) {
                    Icon(Icons.Default.ArrowForwardIos, contentDescription = "Next Month")
                }
            }

            // Export + Expand week
            Row {
                IconButton(onClick = { showExportDialog = true }) {
                    Icon(
                        Icons.Default.Upcoming,
                        "Export",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
                IconButton(onClick = {
                    timeline = !timeline
                    expandedWeekIndex =
                        if (expandedWeekIndex == null) selectedDate.weekOfMonth() else null
                    expandedWeekIndex?.let { week ->
                        coroutineScope.launch {
                            lazyColumnState.animateScrollToItem(
                                week
                            )
                        }
                    }
                }) {
                    Icon(
                        Icons.Default.Expand,
                        "Expand Week",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        // Days of week header
        val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            daysOfWeek.forEach {
                Text(
                    it, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(sizeOfBox.dp), textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Weeks LazyColumn
        LazyColumn(state = lazyColumnState, modifier = Modifier.fillMaxWidth()) {
            items(totalWeeks) { week ->
                if (expandedWeekIndex == null || expandedWeekIndex == week) {
                    val startDayIndex = week * 7 - firstDayIndex + 1
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(7) { dayOfWeek ->
                            val dayIndex = startDayIndex + dayOfWeek
                            if (dayIndex in 1..daysInMonth) {
                                val date = yearMonth.atDay(dayIndex)
                                val todosForDay = todos.filter { it.overlapsDay(date) }
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
                                            2.dp,
                                            MaterialTheme.colorScheme.tertiary,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            selectedDateEpoch = date.toEpochDay()
                                            expandedWeekIndex = week
                                            coroutineScope.launch {
                                                lazyColumnState.animateScrollToItem(
                                                    week
                                                )
                                            }
                                        }
                                ) {
                                    Text(
                                        dayIndex.toString(), fontSize = 14.sp,
                                        color = if (date.toEpochDay() == selectedDateEpoch || date == today)
                                            MaterialTheme.colorScheme.inversePrimary
                                        else MaterialTheme.colorScheme.primary
                                    )
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
                                                            ToDoState.IN_PROGRESS -> Color(
                                                                0xFFFFC107
                                                            )

                                                            else -> Color.Gray
                                                        }
                                                    )
                                            )
                                        }
                                    }
                                }
                            } else Box(modifier = Modifier.size(sizeOfBox.dp)) {}
                        }
                    }
                }
            }
        }

        // Prev/Next week buttons (collapsed mode)
        if (timeline) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {
                    expandedWeekIndex = ((expandedWeekIndex ?: 0) - 1).coerceAtLeast(0)
                    coroutineScope.launch { lazyColumnState.animateScrollToItem(expandedWeekIndex!!) }
                }) {
                    Icon(
                        Icons.Default.ArrowBackIosNew,
                        "Prev Week",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }

                IconButton(onClick = {
                    expandedWeekIndex = ((expandedWeekIndex ?: 0) + 1).coerceAtMost(totalWeeks - 1)
                    coroutineScope.launch { lazyColumnState.animateScrollToItem(expandedWeekIndex!!) }
                }) {
                    Icon(
                        Icons.Default.ArrowForwardIos,
                        "Next Week",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom panel tasks
        val tasksForDay = todos.filter { it.overlapsDay(selectedDate) }.toMutableList()
        if (tasksForDay.isNotEmpty()) {
            Text(
                "Tasks for ${selectedDate.dayOfMonth}/${selectedDate.monthValue}/${selectedDate.year}",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (timeline) {
                // Use your swipeable card stack
                TinderCardStack(
                    cards = tasksForDay,
                    maxVisible = 3,
                    viewModel = viewModel,
                    onSwiped = { todo, direction ->

                        // Optional: you can update your database or show toast
                        // For demo: just show which card was swiped
//                        Toast.makeText(
//                            LocalContext.current,
//                            "Swiped ${todo.title} ${direction.name}",
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }
                )
            } else {
                ToDoListScreenForDay(tasksForDay, viewModel)
            }
        } else {
            Text("No tasks for this day", fontSize = 14.sp, color = Color.Gray)
        }


    }

    // Export Dialog
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
                                exportAsIcsWithTasks(context, viewModel.todosWithTasks.value)
                            }
                            .padding(8.dp)
                    )
                }
            },
            dismissButton = {
                Text(
                    "Cancel",
                    modifier = Modifier.clickable { showExportDialog = false })
            }
        )
    }
}

@Composable
fun ScrollableTaskTimeline(tasks: List<ToDo>) {
    val hourHeightDp = 80.dp
    val hours = (0..23).toList()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(hours) { hour ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(hourHeightDp)
                        .background(MaterialTheme.colorScheme.background)
                        .border(0.5.dp, Color.LightGray)
                ) {
                    Text(
                        text = String.format("%02d:00", hour),
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 8.dp),
                        color = Color.Gray,
                        fontSize = 14.sp
                    )

                    val overlappingTasks = tasks.filter { task ->
                        val start = task.createdAt.toLocalDateTime()
                        val end = start.plusMinutes(task.durationMinutes?.toLong() ?: 60)
                        val hourStart = start.withHour(hour).withMinute(0).withSecond(0)
                        val hourEnd = hourStart.plusHours(1)
                        end.isAfter(hourStart) && start.isBefore(hourEnd)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 70.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        overlappingTasks.forEach { task ->
                            val start = task.createdAt.toLocalDateTime()
                            val end = start.plusMinutes(task.durationMinutes?.toLong() ?: 60)
                            val hourStart = start.withHour(hour).withMinute(0).withSecond(0)
                            val hourEnd = hourStart.plusHours(1)

                            val overlapStart = if (start.isAfter(hourStart)) start else hourStart
                            val overlapEnd = if (end.isBefore(hourEnd)) end else hourEnd

                            val startOffset =
                                ((overlapStart.minute + overlapStart.second / 60f) / 60f) * hourHeightDp.value
                            val durationHeight =
                                ((overlapEnd.hour * 60 + overlapEnd.minute) - (overlapStart.hour * 60 + overlapStart.minute)) / 60f * hourHeightDp.value

                            Box(
                                modifier = Modifier
                                    .width(180.dp)
                                    .height(durationHeight.dp)
                                    .offset(y = startOffset.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(task.cardColor.listColor[2])
                                    .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .align(Alignment.CenterStart)
                                ) {
                                    Text(
                                        task.title,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        "${start.hour}:${
                                            start.minute.toString().padStart(2, '0')
                                        } - ${end.hour}:${end.minute.toString().padStart(2, '0')}",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToDoListScreenForDay(tasks: List<ToDo>, viewModel: ToDoViewModel) {
    val context = LocalContext.current
    val selectedToDo by viewModel.selectedToDo.collectAsState()
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(tasks, key = { it.id }) { todo ->
            CardStickyNote(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(top = 16.dp)
                    .clickable {
                        if (todo.locked) {
                            BiometricHelper(
                                context as FragmentActivity,
                                onSuccess = { viewModel.selectToDo(todo) },
                                onError = { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }).authenticate()
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
                onClick = {},
                isLocked = todo.locked
            )
        }
    }
    selectedToDo?.let { todo ->
        TaskDialog(
            todoTitle = todo.title,
            todoId = todo.id,
            viewModel = viewModel,
            onAddSubTask = { viewModel.addTask(todo.id, it) },
            onUpdateSubTask = { viewModel.updateTask(it) },
            onDeleteSubTask = { viewModel.deleteTask(it) },
            onDismiss = { viewModel.clearSelectedToDo() }
        )
    }
}
