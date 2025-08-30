package com.ibrahim.to_dolist.core.utility

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.ibrahim.to_dolist.data.model.ToDoWithTasks
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.Q)
fun exportAsIcsWithTasks(context: Context, todosWithTasks: List<ToDoWithTasks>) {
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
    val builder = StringBuilder()
    builder.appendLine("BEGIN:VCALENDAR")
    builder.appendLine("VERSION:2.0")
    builder.appendLine("PRODID:-//ToDoListApp//EN")

    todosWithTasks.forEach { todoWithTasks ->
        val todo = todoWithTasks.todo
        val start = todo.createdAt.toLocalDateTime()
        val duration = todo.durationMinutes ?: 60
        val end = start.plusMinutes(duration.toLong())
        val uid = "${todo.id}@todolist.app"

        // Encode description and title for ICS
        val titleEscaped = escapeICSText(todo.title)
        val tasksDescription = if (todoWithTasks.tasks.isNotEmpty()) {
            todoWithTasks.tasks.joinToString(separator = "\\n") { "- ${escapeICSText(it.text)} [${if (it.isChecked) "Done" else "Pending"}]" }
        } else {
            "No subtasks"
        }

        builder.appendLine("BEGIN:VEVENT")
        builder.appendLine("UID:$uid")
        builder.appendLine("DTSTAMP:${start.atOffset(ZoneOffset.UTC).format(formatter)}")
        builder.appendLine("DTSTART:${start.atOffset(ZoneOffset.UTC).format(formatter)}")
        builder.appendLine("DTEND:${end.atOffset(ZoneOffset.UTC).format(formatter)}")
        builder.appendLine("SUMMARY:$titleEscaped")
        builder.appendLine("DESCRIPTION:Task from ToDo App\\n$tasksDescription")
        builder.appendLine("END:VEVENT")
    }

    builder.appendLine("END:VCALENDAR")
    builder.appendLine() // important

    saveFile(context, "todos_export_with_tasks.ics", builder.toString(), "text/calendar")
}

// Escape special chars for ICS format
fun escapeICSText(text: String): String {
    return text.replace("\\", "\\\\")
        .replace("\n", "\\n")
        .replace(";", "\\;")
        .replace(",", "\\,")
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun saveFile(context: Context, fileName: String, content: String, mimeType: String = "text/plain") {
    val resolver = context.contentResolver
    val values = ContentValues().apply {
        put(android.provider.MediaStore.Downloads.DISPLAY_NAME, fileName)
        put(android.provider.MediaStore.Downloads.MIME_TYPE, mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(android.provider.MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
    }

    val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
    if (uri != null) {
        resolver.openOutputStream(uri).use { out: OutputStream? ->
            out?.write(content.toByteArray())
        }
        Toast.makeText(context, "Saved to Downloads/$fileName", Toast.LENGTH_LONG).show()
    } else {
        Toast.makeText(context, "Failed to save file", Toast.LENGTH_LONG).show()
    }
}

/**
 * Export the Room database file to Downloads
 * @param context: Context
 * @param databaseName: the Room database filename (e.g., "todos.db")
 */
fun exportDatabase(context: Context, databaseName: String) {
    try {
        val dbFile = context.getDatabasePath(databaseName)

        if (!dbFile.exists()) {
            Toast.makeText(context, "Database file not found", Toast.LENGTH_SHORT).show()
            return
        }else{
            Toast.makeText(context, "$dbFile", Toast.LENGTH_SHORT).show()
        }

        val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloads.exists()) downloads.mkdirs()

        val backupFile = File(downloads, "backup_$databaseName")

        FileInputStream(dbFile).use { input ->

            FileOutputStream(backupFile).use { output ->
                input.copyTo(output)
            }
        }

        Toast.makeText(context, "Database exported to Downloads/${backupFile.name}", Toast.LENGTH_LONG).show()
    } catch (e: IOException) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to export database: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
