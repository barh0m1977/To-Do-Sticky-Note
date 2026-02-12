package com.ibrahim.to_dolist.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ibrahim.to_dolist.data.model.Tasks
import com.ibrahim.to_dolist.data.model.ToDo
import com.ibrahim.to_dolist.data.model.ToDoWithTasks
import kotlinx.coroutines.flow.Flow

@Dao
interface ToDoDao {

    @Query("SELECT * FROM todos")
    fun getAll(): Flow<List<ToDo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: ToDo)

    @Delete
    suspend fun delete(todo: ToDo)

    @Update
    suspend fun update(todo: ToDo)

    // --- SubTask Operations ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubTask(subTask: Tasks)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReturnId(todo: ToDo): Long

    @Update
    suspend fun updateSubTask(subTask: Tasks)

    @Delete
    suspend fun deleteSubTask(subTask: Tasks)

    @Query("SELECT * FROM tasks WHERE todoId = :todoId")
    fun getTasksForTodo(todoId: Int): Flow<List<Tasks>>

    // --- ToDo with Tasks (One-to-Many) ---
    @Transaction
    @Query("SELECT * FROM todos")
    fun getToDosWithTasks(): Flow<List<ToDoWithTasks>>

    @Query("SELECT * FROM tasks WHERE todoId = :todoId")
    fun getTasksForTodoFlow(todoId: Int): Flow<List<Tasks>>

}
