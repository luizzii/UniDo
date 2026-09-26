package com.example.unido.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.unido.data.local.Task
import com.example.unido.data.repository.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    val tasks: StateFlow<List<Task>> = repository.tasks.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    fun addTask(title: String, subject: String, dateTime: String, description: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.insert(
                Task(
                    title = title.trim(),
                    subject = subject.trim().ifBlank { "Sem disciplina" },
                    createdBy = "Criado pelo aluno",
                    dateTime = dateTime.trim().ifBlank { "Data não informada" },
                    description = description.trim().ifBlank { "Sem descrição" }
                )
            )
        }
    }

    fun completeTask(task: Task) {
        viewModelScope.launch { repository.update(task.copy(completed = true)) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { repository.delete(task) }
    }

    suspend fun getById(id: Int): Task? = repository.getById(id)

    companion object {
        fun factory(repository: TaskRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    TaskViewModel(repository) as T
            }
    }
}
