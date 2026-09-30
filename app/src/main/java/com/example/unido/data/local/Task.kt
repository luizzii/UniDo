package com.example.unido.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Table and column names keep the original schema so existing databases still work.
@Entity(tableName = "Tarefa")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "titulo") val title: String,
    @ColumnInfo(name = "disciplina") val subject: String,
    @ColumnInfo(name = "criadoPor") val createdBy: String,
    @ColumnInfo(name = "dataHora") val dateTime: String,
    @ColumnInfo(name = "descricao") val description: String,
    @ColumnInfo(name = "concluida") val completed: Boolean = false
)
