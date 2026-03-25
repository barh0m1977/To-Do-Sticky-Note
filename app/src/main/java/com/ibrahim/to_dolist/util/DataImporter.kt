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

// ─── Import Result ────────────────────────────────────────────────────────────

/**
 * Summary returned after [DataImporter.importToDatabase] completes.
 *
 * @param insertedTodos  Number of new to-do lists created.
 * @param skippedTodos   Number of to-do lists that already existed (matched by title + color).
 * @param insertedTasks  Number of new tasks inserted across all lists.
 * @param skippedTasks   Number of tasks skipped because they already existed under a matched list.
 */
data class ImportResult(
    val insertedTodos : Int,
    val skippedTodos  : Int,
    val updatedTodos  : Int,
    val insertedTasks : Int,
    val skippedTasks  : Int,
) {
    val totalInserted : Int get() = insertedTodos + insertedTasks
    val totalSkipped  : Int get() = skippedTodos  + skippedTasks
    val totalUpdated  : Int get() = updatedTodos

    fun toMessage(): String = buildString {
        if (totalInserted > 0) append("$totalInserted item${if (totalInserted > 1) "s" else ""} imported. ")
        if (totalUpdated  > 0) append("$totalUpdated list${if (totalUpdated  > 1) "s" else ""} updated. ")
        if (totalSkipped  > 0) append("$totalSkipped skipped (already exist).")
        if (totalInserted == 0 && totalUpdated == 0 && totalSkipped == 0) append("Nothing to import.")
    }
}

// ─── Importer ─────────────────────────────────────────────────────────────────

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
                // parts[5] = createdAt, parts[6] = modifiedAt — intentionally unused;
                // imported items always get fresh timestamps from Room.
                val taskText    = parts.getOrElse(8) { "" }
                val taskChecked = parts.getOrElse(9) { "false" }.toBoolean()

                val color = ToDoStickyColors.entries.find { it.name == cardColorRaw }
                    ?: ToDoStickyColors.SUNRISE
                val state = ToDoState.entries.find { it.name == stateRaw }
                    ?: ToDoState.PENDING
                val locked = lockedRaw.equals("true", ignoreCase = true)

                val entry = map.getOrPut(todoId) {
                    ToDoWithTasks(
                        todo = ToDo(
                            id        = 0, // Room will generate a new id
                            title     = todoTitle,
                            cardColor = color,
                            state     = state,
                            locked    = locked,
                            // createdAt/modifiedAt intentionally omitted — Room defaults apply
                        ),
                        tasks = mutableListOf(),
                    )
                }

                if (taskText.isNotBlank()) {
                    (entry.tasks as MutableList).add(
                        Tasks(
                            id        = 0, // Room will generate
                            todoId    = 0, // patched in importToDatabase
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
     * Inserts [todosWithTasks] into Room with full deduplication:
     *
     * • A to-do is considered a duplicate when an existing row matches on
     *   both **title** (case-insensitive, trimmed) and **cardColor**.
     *
     * • When a duplicate to-do is found, its tasks are still checked
     *   individually — only tasks whose text doesn't already exist under
     *   that list are inserted.
     *
     * • Brand-new to-dos (no match) are inserted in full.
     *
     * Original IDs from the file are always discarded to avoid primary-key
     * conflicts with existing data.
     *
     * @return [ImportResult] summarising what was inserted vs skipped.
     */
    suspend fun importToDatabase(
        context        : Context,
        todosWithTasks : List<ToDoWithTasks>,
        updateExisting : Boolean = false, // ← false = safe default, true = overwrite
    ): ImportResult = withContext(Dispatchers.IO) {
        val dao = ToDoDatabase.getDatabase(context).toDoDao()
        val existingTodos = dao.getAllTodos()

        var insertedTodos = 0
        var skippedTodos  = 0
        var updatedTodos  = 0
        var insertedTasks = 0
        var skippedTasks  = 0

        todosWithTasks.forEach { todoWithTasks ->
            val incoming = todoWithTasks.todo

            val existingTodo = existingTodos.find { existing ->
                existing.title.trim().equals(incoming.title.trim(), ignoreCase = true) &&
                        existing.cardColor == incoming.cardColor
            }

            if (existingTodo != null) {
                // ── Matched — optionally overwrite the todo fields ────────────────
                if (updateExisting) {
                    dao.update(
                        incoming.copy(
                            id = existingTodo.id // ✅ keep the real DB id, update everything else
                        )
                    )
                    updatedTodos++
                } else {
                    skippedTodos++
                }

                // Either way — still deduplicate tasks
                val existingTaskTexts = dao
                    .getTasksForTodoOnce(existingTodo.id)
                    .map { it.text.trim().lowercase() }
                    .toHashSet()

                todoWithTasks.tasks.forEach { task ->
                    if (task.text.trim().lowercase() in existingTaskTexts) {
                        skippedTasks++
                    } else {
                        dao.insertSubTask(task.copy(id = 0, todoId = existingTodo.id))
                        insertedTasks++
                    }
                }
            } else {
                // ── Brand new — insert everything ─────────────────────────────────
                insertedTodos++
                val generatedId = dao.insertReturnId(incoming.copy(id = 0))
                todoWithTasks.tasks.forEach { task ->
                    dao.insertSubTask(task.copy(id = 0, todoId = generatedId.toInt()))
                    insertedTasks++
                }
            }
        }

        ImportResult(
            insertedTodos = insertedTodos,
            skippedTodos  = skippedTodos,
            updatedTodos  = updatedTodos,
            insertedTasks = insertedTasks,
            skippedTasks  = skippedTasks,
        )
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
                ch == '"' && inQuotes  -> {
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