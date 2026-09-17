package com.example.unido.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TarefaDao {
    @Query("SELECT * FROM Tarefa ORDER BY id DESC")
    fun listarTarefas(): Flow<List<Tarefa>>
    

    @Query("SELECT * FROM Tarefa WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Tarefa?

    @Insert
    suspend fun inserir(tarefa: Tarefa)

    @Update
    suspend fun atualizar(tarefa: Tarefa)

    @Delete
    suspend fun deletar(tarefa: Tarefa)
}
