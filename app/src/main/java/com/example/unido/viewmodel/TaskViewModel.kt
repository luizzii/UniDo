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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    private val _isReady = MutableStateFlow(false)

    /** Becomes true once the first task list has been read from the database. */
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    // Started eagerly so the splash screen can wait for the first read without a second query.
    val tasks: StateFlow<List<Task>> = repository.tasks
        .onEach { _isReady.value = true }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun addTask(title: String, subject: String, dueAt: Long?, description: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.insert(
                Task(
                    title = title.trim(),
                    subject = subject.trim(),
                    createdBy = CREATED_BY_STUDENT,
                    dueAt = dueAt,
                    description = description.trim()
                )
            )
        }
    }

    fun updateTask(task: Task, title: String, subject: String, dueAt: Long?, description: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.update(
                task.copy(
                    title = title.trim(),
                    subject = subject.trim(),
                    dueAt = dueAt,
                    description = description.trim()
                )
            )
        }
    }

    fun setCompleted(task: Task, completed: Boolean) {
        viewModelScope.launch { repository.update(task.copy(completed = completed)) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { repository.delete(task) }
    }

    /** Observes a single task, so the screen updates as soon as it changes (e.g. after completing it). */
    fun getTask(id: Int): Flow<Task?> = repository.getById(id)

    companion object {
        // Stored with the task, so it stays here instead of in the string resources.
        private const val CREATED_BY_STUDENT = "Criado pelo aluno"

        fun factory(repository: TaskRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(TaskViewModel::class.java)) {
                        "Unknown ViewModel class: ${modelClass.name}"
                    }
                    return TaskViewModel(repository) as T
                }
            }
    }
}
