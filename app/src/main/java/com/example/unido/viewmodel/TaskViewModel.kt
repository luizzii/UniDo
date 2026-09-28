package com.example.unido.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.unido.data.local.Task
import com.example.unido.data.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    val tasks: StateFlow<List<Task>> = repository.tasks.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    private val _isReady = MutableStateFlow(false)

    /** Becomes true once the first task list has been read from the database. */
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    init {
        viewModelScope.launch {
            repository.tasks.first()
            _isReady.value = true
        }
    }

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

    /** Observes a single task, so the screen updates as soon as it changes (e.g. after completing it). */
    fun getTask(id: Int): Flow<Task?> = repository.getById(id)

    companion object {
        fun factory(repository: TaskRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    TaskViewModel(repository) as T
            }
    }
}
