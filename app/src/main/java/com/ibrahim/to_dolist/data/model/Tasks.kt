package com.ibrahim.to_dolist.data.model

import androidx.room.*

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
data class Tasks(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val todoId: Int,
    val text: String,
    val isChecked: Boolean = false
)
