package com.example.unido.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Tarefa(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val disciplina: String,
    val criadoPor: String,
    val dataHora: String,
    val descricao: String,
    val concluida: Boolean = false
)
