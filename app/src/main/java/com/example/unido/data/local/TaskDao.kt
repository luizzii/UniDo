package com.example.unido.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    // Open tasks first, nearest deadline on top; tasks without a deadline come after the dated ones.
    @Query("SELECT * FROM Tarefa ORDER BY concluida ASC, (prazo IS NULL) ASC, prazo ASC, id DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM Tarefa WHERE id = :id LIMIT 1")
    fun getById(id: Int): Flow<Task?>

    @Insert
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)
}
