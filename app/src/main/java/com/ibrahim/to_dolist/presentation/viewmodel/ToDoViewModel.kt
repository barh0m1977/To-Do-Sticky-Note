package com.ibrahim.to_dolist.presentation.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ibrahim.to_dolist.data.db.ToDoDatabase
import com.ibrahim.to_dolist.data.model.ToDo
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ToDoViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = ToDoDatabase.getDatabase(application).toDoDao()

    private val _todos = MutableStateFlow<List<ToDo>>(emptyList())
    val todos: StateFlow<List<ToDo>> = _todos.asStateFlow()

    init {
        loadTodos()
    }

    fun loadTodos() = viewModelScope.launch {
        val list = dao.getAll()
        _todos.value = list
    }

    fun addToDo(todo: ToDo) = viewModelScope.launch {
        dao.insert(todo)
        loadTodos()
    }

    fun deleteToDo(todo: ToDo) = viewModelScope.launch {
        dao.delete(todo)
        loadTodos()
    }

    fun deleteToDoLocal(todo: ToDo) {
        _todos.update { currentList ->
            currentList.filterNot { it.id == todo.id }
        }
        viewModelScope.launch {
            dao.delete(todo)
        }
    }
}

