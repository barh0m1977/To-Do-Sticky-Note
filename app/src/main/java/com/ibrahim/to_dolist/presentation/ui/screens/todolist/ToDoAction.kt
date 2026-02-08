package com.ibrahim.to_dolist.presentation.ui.screens.todolist

import com.ibrahim.to_dolist.data.model.ToDo

sealed class ToDoAction {
    data class RequestBiometric(val todo: ToDo) : ToDoAction()
    data class OpenTodo(val todo: ToDo) : ToDoAction()
    data class ShowMessage(val message: String) : ToDoAction()
}