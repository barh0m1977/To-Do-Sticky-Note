package com.ibrahim.to_dolist.data.dao
import androidx.room.*
import com.ibrahim.to_dolist.data.model.ToDo

@Dao
interface ToDoDao {
    @Query("SELECT * FROM todos")
    suspend fun getAll(): List<ToDo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: ToDo)

    @Delete
    suspend fun delete(todo: ToDo)
}