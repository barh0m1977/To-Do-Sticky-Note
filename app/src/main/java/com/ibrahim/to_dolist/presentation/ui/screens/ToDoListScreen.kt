package com.ibrahim.to_dolist.presentation.ui.screens

import CardStickyNote
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ibrahim.to_dolist.R.drawable
import com.ibrahim.to_dolist.data.model.ToDo
import com.ibrahim.to_dolist.presentation.viewmodel.ToDoViewModel
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.collectAsState


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToDoListScreen(viewModel: ToDoViewModel) {
    val todos by viewModel.todos.collectAsState()

    val gridState = rememberLazyGridState()

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(2),
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
                    .animateItemPlacement(),
                text = todo.title,

                onDeleteConfirmed = {
                    viewModel.deleteToDoLocal(todo)
                },
                colorArray = todo.cardColor,
                state = todo.state
            )
        }
    }
}








