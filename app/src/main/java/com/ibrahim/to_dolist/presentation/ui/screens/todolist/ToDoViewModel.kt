package com.ibrahim.to_dolist.presentation.ui.screens.todolist

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ibrahim.to_dolist.data.db.ToDoDatabase
import com.ibrahim.to_dolist.data.model.Tasks
import com.ibrahim.to_dolist.data.model.ToDo
import com.ibrahim.to_dolist.data.model.ToDoWithTasks
import com.ibrahim.to_dolist.presentation.util.SortDirection
import com.ibrahim.to_dolist.presentation.util.SortOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ToDoViewModel(application: Application) : AndroidViewModel(application) {

    private val dao by lazy { ToDoDatabase.Companion.getDatabase(application).toDoDao() }
    private val _action = Channel<ToDoAction>()
    val action = _action.receiveAsFlow()

    private val _todos = MutableStateFlow<List<ToDo>>(emptyList())
    val todos: StateFlow<List<ToDo>> = _todos.asStateFlow()

    private val _todosWithTasks = MutableStateFlow<List<ToDoWithTasks>>(emptyList())
    val todosWithTasks: StateFlow<List<ToDoWithTasks>> = _todosWithTasks.asStateFlow()
    private val _selectedToDo = MutableStateFlow<ToDo?>(null)
    val selectedToDo: StateFlow<ToDo?> = _selectedToDo.asStateFlow()


    var sortOption by mutableStateOf(SortOption.CREATED_DATE)
        private set
    var sortDirection by mutableStateOf(SortDirection.DESCENDING)
        private set


    fun onSortOptionChanged(option: SortOption) {
        sortOption = option
        filterAndSortToDos()
    }
    fun onSortDirectionChanged(direction: SortDirection) {
        sortDirection = direction
        filterAndSortToDos()
    }

    private fun filterAndSortToDos() {
        val allTodos = _todosWithTasks.value.map { it.todo }

        val filtered = when (sortOption) {
            SortOption.ONLY_DONE -> allTodos.filter { it.state.name == "DONE" }
            SortOption.ONLY_PENDING -> allTodos.filter { it.state.name == "PENDING" }
            SortOption.ONLY_IN_PROGRESS -> allTodos.filter { it.state.name == "IN_PROGRESS" }
            SortOption.OPENED -> allTodos.filter { !it.locked }
            SortOption.LOCKED -> allTodos.filter { it.locked }
            else -> allTodos
        }

        _todos.value = when (sortOption) {
            SortOption.CREATED_DATE -> when (sortDirection) {
                SortDirection.DESCENDING -> filtered.sortedByDescending { it.createdAt }
                SortDirection.ASCENDING -> filtered.sortedBy { it.createdAt }
            }
            SortOption.MODIFIED_DATE -> when (sortDirection) {
                SortDirection.DESCENDING -> filtered.sortedByDescending { it.modifiedAt }
                SortDirection.ASCENDING -> filtered.sortedBy { it.modifiedAt }
            }
            else -> filtered
        }
    }

//        private fun sortToDos() {
//            _todos.value = when (sortOption) {
//                SortOption.CREATED_DATE -> _todos.value.sortedByDescending { it.createdAt }
//                SortOption.MODIFIED_DATE -> _todos.value.sortedByDescending { it.modifiedAt }
//                SortOption.OPENED -> _todos.value.sortedBy { it.state.name }
//            }
//        }


    fun onTodoClicked(todo: ToDo) {
        viewModelScope.launch {
            if (todo.locked) {
                _action.send(ToDoAction.RequestBiometric(todo))
            } else {
                _action.send(ToDoAction.OpenTodo(todo))
            }
        }
    }
    fun openAfterBiometric(todo: ToDo) {
        _selectedToDo.value = todo
    }


    fun clearSelectedToDo() {
        _selectedToDo.value = null
    }

    init {
        observeTodos()
    }

    private fun updateModifiedAt(todoId: Int) = viewModelScope.launch {
        val current = _todos.value.find { it.id == todoId }
        current?.let {
            val updated = it.copy(modifiedAt = System.currentTimeMillis())
            dao.update(updated)
        }
    }

    private fun observeTodos() {
        viewModelScope.launch {
            dao.getToDosWithTasks().collect { list ->
                _todosWithTasks.value = list
                filterAndSortToDos()
            }
        }
    }


    fun addToDo(todo: ToDo) = viewModelScope.launch(Dispatchers.IO) {
        dao.insert(todo)
        // No need to manually reload — Flow auto-updates
    }

    fun updateToDo(todo: ToDo) = viewModelScope.launch(Dispatchers.IO) {
        val updatedToDo = todo.copy(modifiedAt = System.currentTimeMillis())
        dao.update(updatedToDo)
    }

    fun deleteToDoLocal(todo: ToDo) {
        _todos.value = _todos.value.filterNot { it.id == todo.id }
        viewModelScope.launch(Dispatchers.IO) {
            dao.delete(todo)
        }
    }

    fun addTask(todoId: Int, text: String) = viewModelScope.launch {
        val task = Tasks(todoId = todoId, text = text)
        dao.insertSubTask(task)
        updateModifiedAt(todoId)

    }

    fun updateTask(tasks: Tasks) = viewModelScope.launch {
        dao.updateSubTask(tasks)
        updateModifiedAt(tasks.todoId)

    }

    fun deleteTask(tasks: Tasks) = viewModelScope.launch {
        dao.deleteSubTask(tasks)
        updateModifiedAt(tasks.todoId)

    }

    fun getTasksFlow(todoId: Int): Flow<List<Tasks>> = dao.getTasksForTodoFlow(todoId)


}