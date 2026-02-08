package com.ibrahim.to_dolist.util

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.ibrahim.to_dolist.data.model.ToDoWithTasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter

object TaskExporter {

    suspend fun exportToCSV(
        context: Context,
        todosWithTasks: List<ToDoWithTasks>,
        fileName: String = "todos.csv"
    ): File = withContext(Dispatchers.IO) {

        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )

        FileWriter(file).use { writer ->
            writer.append("ToDoID,Title,CardColor,State,TaskID,TaskText,TaskChecked\n")

            todosWithTasks.forEach { todoWithTasks ->
                val todo = todoWithTasks.todo

                if (todoWithTasks.tasks.isEmpty()) {
                    writer.append(
                        "${todo.id}," +
                                "${todo.title}," +
                                "${todo.cardColor.name}," +
                                "${todo.state},,,\n"
                    )
                } else {
                    todoWithTasks.tasks.forEach { task ->
                        writer.append(
                            "${todo.id}," +
                                    "${todo.title}," +
                                    "${todo.cardColor.name}," +
                                    "${todo.state}," +
                                    "${task.id}," +
                                    "${task.text}," +
                                    "${task.isChecked}\n"
                        )
                    }
                }
            }
        }
        shareFile(context, file)
        file
    }

    suspend fun exportToJSON(
        context: Context,
        todosWithTasks: List<ToDoWithTasks>,
        fileName: String = "todos.json"
    ): File = withContext(Dispatchers.IO) {

        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )
        val json = Gson().toJson(todosWithTasks)

        FileWriter(file).use { writer ->
            writer.write(json)
        }
        shareFile(context, file)
        file
    }
}

fun shareFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
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

