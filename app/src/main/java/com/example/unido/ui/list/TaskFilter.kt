package com.example.unido.ui.list

import com.example.unido.data.local.Task
import com.example.unido.util.isOverdue

/** Status filter from the filter button on the list screen. */
enum class TaskFilter(val label: String) {
    ALL("Todas"),
    PENDING("Pendentes"),
    OVERDUE("Atrasadas"),
    COMPLETED("Concluídas");

    fun matches(task: Task, now: Long): Boolean = when (this) {
        ALL -> true
        PENDING -> !task.completed && !task.isOverdue(now)
        OVERDUE -> task.isOverdue(now)
        COMPLETED -> task.completed
    }
}
