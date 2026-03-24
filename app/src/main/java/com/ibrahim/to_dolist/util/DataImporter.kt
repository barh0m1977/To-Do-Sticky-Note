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

    // ── JSON Import ───────────────────────────────────────────────────────────

    suspend fun importFromJSON(context: Context, file: File): List<ToDoWithTasks> =
        withContext(Dispatchers.IO) {
            val json = file.readText()
            Gson().fromJson(json, Array<ToDoWithTasks>::class.java).toList()
        }

    // ── CSV Import ────────────────────────────────────────────────────────────
    // Uses a proper RFC 4180 parser so fields wrapped in quotes (e.g. titles
    // that contain commas) are handled correctly.

    suspend fun importFromCSV(file: File): List<ToDoWithTasks> = withContext(Dispatchers.IO) {
        // Ordered map: original todoId (from file) → ToDoWithTasks being built
        val map = linkedMapOf<String, ToDoWithTasks>()

        BufferedReader(FileReader(file)).use { reader ->
            reader.readLine() // skip header

            reader.forEachLine { line ->
                val parts = parseCsvLine(line)
                // Minimum columns: ToDoID, Title, CardColor, State, Locked, CreatedAt, ModifiedAt
                if (parts.size < 7) return@forEachLine

                val todoId       = parts[0]
                val todoTitle    = parts[1]
                val cardColorRaw = parts[2]
                val stateRaw     = parts[3]
                val lockedRaw    = parts[4]
                // parts[5] = createdAt, parts[6] = modifiedAt — read but not reused
                //   so that imported items always get fresh timestamps from Room
                val taskId       = parts.getOrElse(7) { "" }
                val taskText     = parts.getOrElse(8) { "" }
                val taskChecked  = parts.getOrElse(9) { "false" }.toBoolean()

                val color = ToDoStickyColors.entries.find { it.name == cardColorRaw }
                    ?: ToDoStickyColors.SUNRISE
                val state = ToDoState.entries.find { it.name == stateRaw }
                    ?: ToDoState.PENDING
                val locked = lockedRaw.equals("true", ignoreCase = true)

                val entry = map.getOrPut(todoId) {
                    ToDoWithTasks(
                        todo = ToDo(
                            id         = 0,           // Room will generate a new id
                            title      = todoTitle,
                            cardColor  = color,
                            state      = state,
                            locked     = locked,
                            // createdAt/modifiedAt intentionally omitted — Room defaults apply
                        ),
                        tasks = mutableListOf(),
                    )
                }

                if (taskText.isNotBlank()) {
                    (entry.tasks as MutableList).add(
                        Tasks(
                            id        = 0,    // Room will generate
                            todoId    = 0,    // will be patched in importToDatabase
                            text      = taskText,
                            isChecked = taskChecked,
                        )
                    )
                }
            }
        }

        map.values.toList()
    }

    // ── Save to Database ──────────────────────────────────────────────────────

    /**
     * Inserts all [todosWithTasks] into Room, generating new IDs for every row.
     * The original IDs from the file are intentionally discarded to avoid
     * primary-key conflicts with existing data.
     */
    suspend fun importToDatabase(
        context        : Context,
        todosWithTasks : List<ToDoWithTasks>,
    ) = withContext(Dispatchers.IO) {
        val dao = ToDoDatabase.getDatabase(context).toDoDao()

        todosWithTasks.forEach { todoWithTasks ->
            // Force id = 0 so Room auto-generates a fresh primary key
            val generatedId = dao.insertReturnId(todoWithTasks.todo.copy(id = 0))

            todoWithTasks.tasks.forEach { task ->
                dao.insertSubTask(task.copy(id = 0, todoId = generatedId.toInt()))
            }
        }
    }

    // ── RFC 4180 CSV line parser ──────────────────────────────────────────────
    // Handles:
    //  • Plain fields:  hello,world
    //  • Quoted fields: "hello, world","she said ""hi"""
    //  • Empty fields:  a,,c

    private fun parseCsvLine(line: String): List<String> {
        val result  = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' && !inQuotes -> {
                    inQuotes = true
                }
                ch == '"' && inQuotes -> {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        // Escaped double-quote inside a quoted field
                        current.append('"')
                        i++ // skip the second quote
                    } else {
                        inQuotes = false
                    }
                }
                ch == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> current.append(ch)
            }
            i++
        }

        result.add(current.toString()) // last field
        return result
    }
}