package com.ibrahim.to_dolist.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    foreignKeys = [ForeignKey(
        entity = ToDo::class,
        parentColumns = ["id"],
        childColumns = ["todoId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("todoId")]
)
@Immutable
data class Tasks(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val todoId: Int,
    val text: String,
    val isChecked: Boolean = false
)
