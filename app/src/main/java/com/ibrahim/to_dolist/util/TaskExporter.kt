package com.ibrahim.to_dolist.util

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import com.google.gson.GsonBuilder
import com.ibrahim.to_dolist.data.model.ToDoWithTasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter

object TaskExporter {

    // ── CSV ───────────────────────────────────────────────────────────────────

    suspend fun exportToCSV(
        context         : Context,
        todosWithTasks  : List<ToDoWithTasks>,
        fileName        : String = "todos_${System.currentTimeMillis()}.csv",
    ): File = withContext(Dispatchers.IO) {

        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )

        FileWriter(file).use { writer ->
            // Header
            writer.append("ToDoID,Title,CardColor,State,Locked,CreatedAt,ModifiedAt,TaskID,TaskText,TaskChecked\n")

            todosWithTasks.forEach { todoWithTasks ->
                val todo = todoWithTasks.todo

                if (todoWithTasks.tasks.isEmpty()) {
                    // Write the todo row with empty task columns
                    writer.append(buildCsvRow(
                        todo.id.toString(),
                        todo.title,
                        todo.cardColor.name,
                        todo.state.name,
                        todo.locked.toString(),
                        todo.createdAt.toString(),
                        todo.modifiedAt.toString(),
                        "",
                        "",
                        "",
                    ))
                } else {
                    todoWithTasks.tasks.forEach { task ->
                        writer.append(buildCsvRow(
                            todo.id.toString(),
                            todo.title,
                            todo.cardColor.name,
                            todo.state.name,
                            todo.locked.toString(),
                            todo.createdAt.toString(),
                            todo.modifiedAt.toString(),
                            task.id.toString(),
                            task.text,
                            task.isChecked.toString(),
                        ))
                    }
                }
            }
        }

        shareFile(context, file)
        file
    }

    // ── JSON ──────────────────────────────────────────────────────────────────

    suspend fun exportToJSON(
        context         : Context,
        todosWithTasks  : List<ToDoWithTasks>,
        fileName        : String = "todos_${System.currentTimeMillis()}.json",
    ): File = withContext(Dispatchers.IO) {

        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )

        // Pretty-print so the file is human-readable and diff-friendly
        val gson = GsonBuilder().setPrettyPrinting().create()
        FileWriter(file).use { writer ->
            writer.write(gson.toJson(todosWithTasks))
        }

        shareFile(context, file)
        file
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * RFC 4180-compliant CSV row builder.
     * Wraps any field that contains a comma, double-quote, or newline in
     * double quotes and escapes internal double-quotes by doubling them.
     * This prevents import failures when todo titles contain commas.
     */
    private fun buildCsvRow(vararg fields: String): String =
        fields.joinToString(",", postfix = "\n") { field ->
            if (field.contains(',') || field.contains('"') || field.contains('\n')) {
                "\"${field.replace("\"", "\"\"")}\""
            } else {
                field
            }
        }
}

// ── Share sheet ───────────────────────────────────────────────────────────────

fun shareFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file,
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "*/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    context.startActivity(
        Intent.createChooser(intent, "Export file").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    )
}