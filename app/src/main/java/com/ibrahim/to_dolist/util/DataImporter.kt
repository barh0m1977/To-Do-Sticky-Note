package com.ibrahim.to_dolist.util

import android.content.Context
import com.google.gson.Gson
import com.ibrahim.to_dolist.data.db.ToDoDatabase
import com.ibrahim.to_dolist.data.model.Tasks
import com.ibrahim.to_dolist.data.model.ToDo
import com.ibrahim.to_dolist.data.model.ToDoState
import com.ibrahim.to_dolist.data.model.ToDoStickyColors
import com.ibrahim.to_dolist.data.model.ToDoWithTasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

object DataImporter {

    // ---------- JSON Import ----------
    suspend fun importFromJSON(context: Context, file: File): List<ToDoWithTasks> =
        withContext(Dispatchers.IO) {
            val json = file.readText()
            val gson = Gson()
            val todosWithTasks: Array<ToDoWithTasks> =
                gson.fromJson(json, Array<ToDoWithTasks>::class.java)
            todosWithTasks.toList()
        }

    // ---------- CSV Import ----------
    suspend fun importFromCSV(file: File): List<ToDoWithTasks> = withContext(Dispatchers.IO) {
        val map = mutableMapOf<String, ToDoWithTasks>()

        BufferedReader(FileReader(file)).use { reader ->
            reader.readLine() // skip header
            reader.forEachLine { line ->
                val parts = line.split(",")
                if (parts.size < 7) return@forEachLine

                val todoId = parts[0].toIntOrNull() ?: 0
                val todoTitle = parts[1]
                val cardColorName = parts[2]
                val stateName = parts[3]
                val taskId = parts[4].toIntOrNull() ?: 0
                val taskText = parts[5]
                val taskChecked = parts[6].toBoolean()

                val todoColor = ToDoStickyColors.values().find { it.name == cardColorName }
                    ?: ToDoStickyColors.SUNRISE
                val todoState = ToDoState.values().find { it.name == stateName }
                    ?: ToDoState.PENDING

                val todoWithTasks = map.getOrPut(todoId.toString()) {
                    ToDoWithTasks(
                        todo = ToDo(
                            id = 0, // always let Room auto-generate new ID
                            title = todoTitle,
                            cardColor = todoColor,
                            state = todoState,
                            locked = false
                        ),
                        tasks = mutableListOf()
                    )
                }

                if (taskText.isNotEmpty()) {
                    (todoWithTasks.tasks as MutableList).add(
                        Tasks(
                            id = 0, // auto-generate
                            todoId = 0, // will be fixed after inserting ToDo
                            text = taskText,
                            isChecked = taskChecked
                        )
                    )
                }
            }
        }

        map.values.toList()
    }

    // ---------- Save to Database ----------
    /**
     * Save a list of ToDoWithTasks into the Room database.
     * Existing ToDos with the same ID will be replaced.
     */
    suspend fun importToDatabase(context: Context, todosWithTasks: List<ToDoWithTasks>) =
        withContext(Dispatchers.IO) {
            val db = ToDoDatabase.getDatabase(context)
            val dao = db.toDoDao()

            todosWithTasks.forEach { todoWithTasks ->
                // Insert the ToDo and get its generated ID
                val todo = todoWithTasks.todo.copy(id = 0) // ensure Room generates new ID
                val generatedId = dao.insertReturnId(todo)

                // Insert tasks with the correct todoId
                todoWithTasks.tasks.forEach { task ->
                    dao.insertSubTask(task.copy(todoId = generatedId.toInt()))
                }
            }
        }
}
