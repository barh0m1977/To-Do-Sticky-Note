package com.ibrahim.to_dolist.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ibrahim.to_dolist.data.model.ToDo
import com.ibrahim.to_dolist.presentation.ui.component.cardStyle.ToDoCard
import com.ibrahim.to_dolist.presentation.ui.screens.todolist.ToDoViewModel

// Define custom colors based on the screenshot
val LightGreen = Color(0xFFE0F7FA)
val DarkGreen = Color(0xFF00C853)
val LightBlue = Color(0xFFE3F2FD)
val DarkBlue = Color(0xFF2196F3)
val LightGray = Color(0xFFF5F5F5)
val DarkGray = Color(0xFF9E9E9E)
val TextGray = Color(0xFF616161)
val BackgroundColor = Color(0xFFF0F4F8) // A light background color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(viewModel: ToDoViewModel,todoId: Int) {

    val tasks by viewModel
        .getTasksFlow(todoId)
        .collectAsStateWithLifecycle(emptyList())

    Scaffold(
        bottomBar = { TaskBottomNavigationBar() },
        floatingActionButton = { TaskFloatingActionButton() },
        floatingActionButtonPosition = FabPosition.End
    ) {  paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "My Tasks",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            SearchBar()
            Spacer(modifier = Modifier.height(24.dp))
            ActiveTasksSection(tasks)
            Spacer(modifier = Modifier.height(24.dp))
            CompletedTasksSection()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar() {
    var searchText by remember { mutableStateOf("") }
    OutlinedTextField(
        value = searchText,
        onValueChange = { searchText = it },
        placeholder = { Text("Search tasks...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}

@Composable
fun ActiveTasksSection(toDo: ToDo) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ACTIVE TASKS",
                fontSize = 14.sp,
                color = TextGray,
                fontWeight = FontWeight.SemiBold
            )
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = LightGreen)
            ) {
                Text(
                    text = "4 items " ,
                    color = DarkGreen,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        var completed by remember { mutableStateOf(false) }

        ToDoCard(
            title = toDo.title,
            subtitle = toDo.state.name,
            isCompleted = completed,
            onCheckedChange = { completed = it }
        )
    }
}

@Composable
fun CompletedTasksSection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "COMPLETED",
                fontSize = 14.sp,
                color = TextGray,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Clear all",
                color = DarkGray,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        CompletedTaskItem("Weekly Sync Preparation", "Team • Done 2h ago")
        Spacer(modifier = Modifier.height(8.dp))
        CompletedTaskItem("Submit Expense Reports", "Finance • Done yesterday")
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedTaskItem(title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkGray)
                Text(text = subtitle, color = DarkGray, fontSize = 14.sp)
            }
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Completed",
                tint = DarkGreen,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun TaskFloatingActionButton() {
    FloatingActionButton(
        onClick = { /*TODO*/ },
        containerColor = DarkGreen,
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add Task", tint = Color.White)
    }
}

@Composable
fun TaskBottomNavigationBar() {
    NavigationBar(
        containerColor = Color.White,
        modifier = Modifier.height(80.dp) // Adjust height as needed
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { /*TODO*/ },
            icon = { Icon(Icons.Default.TaskAlt, contentDescription = "Tasks", tint = DarkGreen) },
            label = { Text("Tasks", color = DarkGreen) }
        )
        NavigationBarItem(
            selected = false,
            onClick = { /*TODO*/ },
            icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Calendar") },
            label = { Text("Calendar") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { /*TODO*/ },
            icon = { Icon(Icons.Default.Folder, contentDescription = "Projects") },
            label = { Text("Projects") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { /*TODO*/ },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTaskListScreen() {
    TaskListScreen()
}
