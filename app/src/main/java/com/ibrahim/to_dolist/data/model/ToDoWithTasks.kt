package com.ibrahim.to_dolist.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class ToDoWithTasks(
    @Embedded val todo: ToDo,
    @Relation(
        parentColumn = "id",
        entityColumn = "todoId"
    )
    val tasks: List<Tasks>
)
