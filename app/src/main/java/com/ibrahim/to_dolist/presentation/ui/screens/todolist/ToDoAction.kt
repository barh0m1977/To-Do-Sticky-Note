package com.ibrahim.to_dolist.presentation.ui.screens.todolist

import com.ibrahim.to_dolist.data.model.ToDo

sealed class ToDoAction {
    data class RequestBiometric(val todo: ToDo, val afterSuccess: ActionType) : ToDoAction()
    data class RequestConfirm(val todo: ToDo, val type: ActionType) : ToDoAction()
    data class OpenTodo(val todo: ToDo) : ToDoAction()
    data class EditTodo(val todo: ToDo) : ToDoAction()
    data class DeleteTodo(val todo: ToDo) : ToDoAction()
    data class ShowMessage(val message: String) : ToDoAction()
}

enum class ActionType { OPEN, DELETE, EDIT }
