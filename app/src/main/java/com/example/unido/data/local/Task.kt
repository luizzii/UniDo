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
    /** Blank when the user did not inform a subject. */
    @ColumnInfo(name = "disciplina") val subject: String,
    @ColumnInfo(name = "criadoPor") val createdBy: String,
    /** Deadline in epoch milliseconds, or null when the task has no deadline. */
    @ColumnInfo(name = "prazo") val dueAt: Long?,
    /** Blank when the user did not write a description. */
    @ColumnInfo(name = "descricao") val description: String,
    @ColumnInfo(name = "concluida") val completed: Boolean = false
)
