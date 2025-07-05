package com.ibrahim.to_dolist.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ibrahim.to_dolist.domain.model.ToDoState
import com.ibrahim.to_dolist.domain.model.ToDoStickyColors


@Entity(tableName = "todos")
data class ToDo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val cardColor: ToDoStickyColors = ToDoStickyColors.SUNRISE,
    val state: ToDoState = ToDoState.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis()
)


