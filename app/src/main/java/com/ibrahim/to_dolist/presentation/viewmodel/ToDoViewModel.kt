package com.ibrahim.to_dolist.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ibrahim.to_dolist.data.db.ToDoDatabase
import com.ibrahim.to_dolist.data.model.Tasks
import com.ibrahim.to_dolist.data.model.ToDo
import com.ibrahim.to_dolist.data.model.ToDoWithTasks
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ToDoViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = ToDoDatabase.getDatabase(application).toDoDao()

    private val _todos = MutableStateFlow<List<ToDo>>(emptyList())
    val todos: StateFlow<List<ToDo>> = _todos.asStateFlow()

    private val _todosWithTasks = MutableStateFlow<List<ToDoWithTasks>>(emptyList())
    val todosWithTasks: StateFlow<List<ToDoWithTasks>> = _todosWithTasks.asStateFlow()

    init {
        observeTodos()
    }

    private fun observeTodos() {
        viewModelScope.launch {
            dao.getToDosWithTasks().collect { list ->
                _todosWithTasks.value = list
                _todos.value = list.map { it.todo }
            }
        }
    }

    fun addToDo(todo: ToDo) = viewModelScope.launch {
        dao.insert(todo)
        // No need to manually reload — Flow auto-updates
    }

    fun updateToDo(todo: ToDo) = viewModelScope.launch {
        dao.update(todo)
    }

    fun deleteToDo(todo: ToDo) = viewModelScope.launch {
        dao.delete(todo)
    }

    fun deleteToDoLocal(todo: ToDo) {
        _todos.value = _todos.value.filterNot { it.id == todo.id }
        viewModelScope.launch {
            dao.delete(todo)
        }
    }

    fun addTask(todoId: Int, text: String) = viewModelScope.launch {
        val task = Tasks(todoId = todoId, text = text)
        dao.insertSubTask(task)
    }

    fun updateTask(tasks: Tasks) = viewModelScope.launch {
        dao.updateSubTask(tasks)
    }

    fun deleteTask(tasks: Tasks) = viewModelScope.launch {
        dao.deleteSubTask(tasks)
    }

    fun getTasks(todoId: Int): List<Tasks> {
        return _todosWithTasks.value.firstOrNull { it.todo.id == todoId }?.tasks ?: emptyList()
    }
    fun getTasksFlow(todoId: Int): Flow<List<Tasks>> = dao.getTasksForTodoFlow(todoId)

}
